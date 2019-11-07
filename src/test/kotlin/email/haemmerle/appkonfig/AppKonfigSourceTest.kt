package com.adcubum.appconfig

import email.haemmerle.appkonfig.AppConfigEnvironmentVariableSource
import email.haemmerle.appkonfig.AppConfigJsonFileSource
import email.haemmerle.appkonfig.AppConfigSystemPropertySource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.text.Charsets.UTF_8

class AppConfigSourceTest {

    @Test
    fun shouldReadPropertyFromSystemProperty() {
        // prepare
        System.setProperty("property", "value")
        val sut = AppConfigSystemPropertySource()

        // when
        val result = sut.getValue("property")

        // then
        assertThat(result).isEqualTo("value")
    }

    @Test
    fun shouldReadPropertyFromEnvironmentVariable() {
        // prepare
        setEnvironmentVariable("property", "value")
        val sut = AppConfigEnvironmentVariableSource()

        // when
        val result = sut.getValue("property")

        // then
        assertThat(result).isEqualTo("value")
    }

    @Test
    fun shouldReadPropertyFromEnvironmentVariableUppercase() {
        // prepare
        setEnvironmentVariable("NESTED_PROPERTY", "value")
        val sut = AppConfigEnvironmentVariableSource()

        // when
        val result = sut.getValue("nested.property")

        // then
        assertThat(result).isEqualTo("value")
    }
}

class AppConfigFileSourceTest {
    var testfile: File? = null

    @BeforeEach
    internal fun setUp() {
        testfile = createTempFile("jsonFile")
    }

    @Test
    fun shouldReadPropertyFromJsonFile() {
        // prepare
        testfile?.writeText("""{ "property" : "value" }""", UTF_8)
        val sut = AppConfigJsonFileSource(testfile!!.absolutePath)

        // when
        val result = sut.getValue("property")

        // then
        assertThat(result).isEqualTo("value")
    }

    @Test
    fun shouldReturnNullForUnsetPropertyInJsonFile() {
        // prepare
        testfile?.writeText("""{  }""", UTF_8)
        val sut = AppConfigJsonFileSource(testfile!!.absolutePath)

        // when
        val result = sut.getValue("property")

        // then
        assertThat(result).isEqualTo(null)
    }

    @Test
    fun shouldReadNestedPropertyFromJsonFile() {
        // prepare
        testfile?.writeText("""{ "nested" : { "property": "value" } }""", UTF_8)
        val sut = AppConfigJsonFileSource(testfile!!.absolutePath)

        // when
        val result = sut.getValue("nested.property")

        // then
        assertThat(result).isEqualTo("value")
    }

    @Test
    fun shouldReturnNullForAPropertyStartingwithADotFromJsonFile() {
        // prepare
        testfile?.writeText("""{ "nested" : { "property": "value" } }""", UTF_8)
        val sut = AppConfigJsonFileSource(testfile!!.absolutePath)

        // when
        val result = sut.getValue(".property")

        // then
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnNullValuesIfFileDoesNotExist() {
        // prepare
        testfile?.delete()
        val sut = AppConfigJsonFileSource(testfile!!.absolutePath)

        // when
        val result = sut.getValue("property")

        // then
        assertThat(result).isEqualTo(null)
    }


    @AfterEach
    internal fun tearDown() {
        testfile?.delete()
    }
}

@Suppress("UNCHECKED_CAST")
private fun setEnvironmentVariable(name: String, value: String) = try {
    putToEnvironmentField("theEnvironment", name, value)
    putToEnvironmentField("theCaseInsensitiveEnvironment", name, value)
} catch (e: NoSuchFieldException) {
    addToUnmodifieableEnvironmentField(name, value)
}

private fun putToEnvironmentField(fieldName: String, name: String, value: String) {
    val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
    val theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(fieldName)
    theCaseInsensitiveEnvironmentField.isAccessible = true
    val cienv = theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
    cienv.putAll(mapOf(name to value))
}

private fun addToUnmodifieableEnvironmentField(name: String, value: String) {
    Collections::class.java.declaredClasses
            .filter { clazz -> clazz.name == "java.util.Collections\$UnmodifiableMap" }
            .forEach { clazz ->
                val field = clazz.getDeclaredField("m")
                field.isAccessible = true
                val map = field.get(System.getenv()) as MutableMap<String, String>
                map.clear()
                map.putAll(mapOf(name to value))
            }
}