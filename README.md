# AppKonfig library
Library for loading properties from different sources to a model in Kotlin

## Usage

To use this library apply the following plugins block to your gradle dependencies

```gradle
compile 'email.haemmerle.appkonfig:lib-app-konfig:15cc209'
```

### Configuration 

Configure your application config by creating your AppKonfig object

```
val appProperties by lazy {
    AppKonfig().withSystemProperties().withEnvironment().withJsonFile("application.json")
            .get<ApplicationProperties>()
}
```

`by lazy` will avoid loading the properties when they are not needed.
`.withSystemProperties()` configures the properties to be read from JVM system properties
`.withEnvironment()` configures the properties to be read from Environment variables
`.withJsonFile()` configures the properties to be read from a JSON file. It is searched in the working directory.

You can chain as many property sources as you wish. 
The AppKonfig will try to find each value in the first one. 
When it is not available in this source, it will continue with the next source and so on.

### Defining the model
The properties will be loaded into a model defined with data classes in kotlin.

For an example we use the following model.

```kotlin
data class ApplicationProperties (val db: DbProperties)
data class DbProperties(val url: String, val username: String, val password: String)
```
  
 When a property with default value is not available in any source, AppKonfig will use the default value.
 When a nullable property is not available in any source, AppKonfig will use null.
 When a non nullable property is not available in any source, AppKonfig will throw an exception.
 
 ### Set properties
 
 The following properties will be loaded into the username field of the upper model:
 * **System Properties**  `-Ddb.username=value` 
 * **Environment Variable** `DB_USERNAME=value`
 * **JSON File**:
     ```json
     {
       "db": {
         "username": "value"
       }
     }
     ```