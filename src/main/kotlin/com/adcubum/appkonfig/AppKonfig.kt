package com.adcubum.appkonfig

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
                else -> addContsructorParamForOther(prefix, param, parametersWithValues)
            }
        }
        return defaultConstructor.callBy(parametersWithValues) as T?:throw ErrorReadingConfig()
    }

    fun addConstructorParamForString(prefix: String?, param: KParameter, parametersWithValues: MutableMap<KParameter, Any?>) {
        val fullName = if (prefix != null) "$prefix.${param.name}" else "${param.name}"
        val value = sources.mapNotNull { source ->
            source.getValue(fullName) }.firstOrNull()
        if ( value == null && !param.isOptional && !param.type.isMarkedNullable) {
            throw RequiredParameterNotSet("${param.type.classifier} $fullName")
        } else if ( value != null || !param.isOptional){
            parametersWithValues.put(param, value)
        }
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
