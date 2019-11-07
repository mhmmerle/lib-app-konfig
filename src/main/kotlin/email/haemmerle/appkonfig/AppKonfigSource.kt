package email.haemmerle.appkonfig

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.File
import java.nio.charset.StandardCharsets

interface AppKonfigSource {
    fun getValue(name: String) : String?
}

class AppConfigJsonFileSource(filename: String) : AppKonfigSource {
    private val json : JsonObject

    init {
        json = if (File(filename).exists())
            Klaxon().parseJsonObject(File(filename).reader(StandardCharsets.UTF_8))
        else
            JsonObject()
    }


    override fun getValue(name: String): String? {
        return getValueFromJsonObject(json, name)
    }

    private fun getValueFromJsonObject(json: JsonObject?, name: String): String? {
        return when {
            json == null -> null
            name.matches(Regex(".+\\..+")) -> getValueFromJsonObject(
                    json[name.substringBefore('.')] as JsonObject?,
                    name.substringAfter('.'))
            else -> json[name] as String?
        }
    }
}

class AppConfigSystemPropertySource : AppKonfigSource {
    override fun getValue(name: String): String? {
        return System.getProperty(name)
    }
}

class AppConfigEnvironmentVariableSource : AppKonfigSource {
    override fun getValue(name: String): String? {
        return System.getenv(name)?:
        System.getenv(toUpperCasePropertyName(name))
    }
}

private fun toUpperCasePropertyName(param: String) =
        param.toUpperCase().replace('.', '_')