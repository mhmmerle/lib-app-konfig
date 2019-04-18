package com.adcubum.appconfig

import com.adcubum.appkonfig.AppKonfig
import com.adcubum.appkonfig.AppKonfigSource
import com.adcubum.appkonfig.RequiredParameterNotSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AppKonfigTest {

    @Test
    fun shouldReturnParameter() {
        //prepare
        data class ConfigClass(val property: String)

        val appConfig  = AppKonfig().withSource(
                TestConfigSource(mapOf("property" to "value"))
        )

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.property).isEqualTo("value")
    }

    @Test
    fun shouldFallbackToNullIfNullableNotSet() {
        //prepare
        data class ConfigClass(val property: String?)

        val appConfig  = AppKonfig().withSource(TestConfigSource()
        )

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.property).isNull()
    }

    @Test
    fun shouldReturnDefaultValueIfOptionalNotSet() {
        //prepare
        data class ConfigClass(val property: String = "default")

        val appConfig  = AppKonfig().withSource(TestConfigSource())

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.property).isEqualTo("default")
    }

    @Test
    fun shouldThrowExceptionIfNonNullableNonOptionalNotSet() {
        //prepare
        data class ConfigClass(val property: String)

        val appConfig  = AppKonfig().withSource(TestConfigSource()
        )

        //when
        Assertions.assertThrows(RequiredParameterNotSet::class.java) {
            appConfig.get<ConfigClass>()
        }
    }

    @Test
    fun shouldReturnNestedPropertyValue() {
        //prepare
        data class NestedConfigClass(val property: String)
        data class ConfigClass(val nested: NestedConfigClass)

        val appConfig  = AppKonfig().withSource(TestConfigSource(mapOf("nested.property" to "value" )))

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.nested.property).isEqualTo("value")
    }

    @Test
    fun shouldReadConfigPropertiesInSequence() {
        //prepare
        data class ConfigClass(val property: String)

        val appConfig  = AppKonfig()
                .withSource(TestConfigSource(mapOf("property" to "value" )))
                .withSource(TestConfigSource(mapOf("property" to "fallback" )))

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.property).isEqualTo("value")
    }

    @Test
    fun shouldReturnPropertyFromSecondSourceIfFirstIsNotSet() {
        //prepare
        data class ConfigClass(val property: String)

        val appConfig  = AppKonfig()
                .withSource(TestConfigSource(mapOf()))
                .withSource(TestConfigSource(mapOf("property" to "fallback" )))

        //when
        val result : ConfigClass = appConfig.get()

        //then
        assertThat(result.property).isEqualTo("fallback")
    }


    class TestConfigSource(val values: Map<String, String> = mapOf()) : AppKonfigSource {
        override fun getValue(name: String): String? {
            return values[name]
        }
    }

}
