package email.haemmerle.appkonfig

import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class AppKonfig {
    val sources = mutableListOf<AppKonfigSource>()

    inline fun <reified T> get(): T {
        return get(T::class as KClass<*>)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(kClass: KClass<*>, prefix : String? = null): T {
        val defaultConstructor = kClass.constructors.toList()[0]
        val parametersWithValues = mutableMapOf<KParameter, Any?> ()

        defaultConstructor.parameters.forEach parameter@{ param ->
            if (param.kind != KParameter.Kind.VALUE) return@parameter

            when (param.type.classifier) {
                String::class -> addConstructorParamForString(prefix, param, parametersWithValues)
                File::class -> addConstructorParamForFile(prefix, param, parametersWithValues, ::File)
                else -> addContsructorParamForOther(prefix, param, parametersWithValues)
            }
        }
        return defaultConstructor.callBy(parametersWithValues) as T?:throw ErrorReadingConfig()
    }

    private fun addConstructorParamForFile(prefix: String?, param: KParameter, parametersWithValues: MutableMap<KParameter, Any?>, factory: ((String) -> (Any))?) {
        val value = getParameterStringValue(prefix, param)
        if ( value == null && param.isOptional){
            return
        }
        if (value == null) {
            parametersWithValues.put(param, null)
        } else if (factory != null){
            parametersWithValues.put(param, factory(value))
        } else {
            parametersWithValues.put(param, value)
        }

    }

    fun addConstructorParamForString(prefix: String?, param: KParameter, parametersWithValues: MutableMap<KParameter, Any?>) {
        val value = getParameterStringValue(prefix, param)
        if ( value == null && param.isOptional){
            return
        }
        parametersWithValues.put(param, value)
    }

    private fun getParameterStringValue(prefix: String?, param: KParameter): String? {
        val fullName = if (prefix != null) "$prefix.${param.name}" else "${param.name}"
        val value = sources.mapNotNull { source -> source.getValue(fullName) }.firstOrNull()
        if (value == null && !param.isOptional && !param.type.isMarkedNullable) {
            throw RequiredParameterNotSet("${param.type.classifier} $fullName")
        }
        return value
    }

    private fun addContsructorParamForOther(prefix: String?, param: KParameter, parametersWithValues: MutableMap<KParameter, Any?>) {
        val newPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
        parametersWithValues.put(param, this.get(param.type.classifier as KClass<*>, newPrefix))
    }

    fun withJsonFile(filename: String): AppKonfig {
        sources.add(AppConfigJsonFileSource(filename))
        return this
    }

    fun withSource(testConfigSource: AppKonfigSource): AppKonfig {
        sources.add(testConfigSource)
        return this
    }

    fun withEnvironment(): AppKonfig {
        sources.add(AppConfigEnvironmentVariableSource())
        return this
    }

    fun withSystemProperties(): AppKonfig {
        sources.add(AppConfigSystemPropertySource())
        return this
    }

}

class ErrorReadingConfig : Throwable()

class RequiredParameterNotSet(parameter: String) : Throwable(parameter)
