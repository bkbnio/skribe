Skribe is the Yin to [Kompendium's](https://bkbn.gitbook.io/kompendium) Yang.  It is a Gradle plugin for 
generating Ktor API Client boilerplate from an OpenAPI specification.  

Unlike other OpenAPI generators, Skribe does not generate an entire app.  Instead, it relies on Kotlin extension
functions to provide type-safe generated functions for each API call.  This allows you to use the generated code
in a highly flexible manner, taking the routes that you wish to leverage, without having to translate them from 
the definition yourself.

{% hint style="danger" %}
Skribe is still _very_ early in development.  There are many OpenAPI features that are not supported, and likely many
bugs in the code generation.  If you encounter any issues, please open an issue [here](https://github.com/bkbnio/skribe/issues/new)
{% endhint %}

## Usage

Skribe is a gradle plugin available via the Gradle Plugin Portal.  As such, it can be easily added to any Gradle project
by declaring it in the plugins block

```kotlin
plugins {
    id("io.bkbn.scribe") version "latest.release"
}
```

To set up the plugin, you can then configure the extension properties

```kotlin
skribe {
  specUrl.set(file("../path/to/spec").absoluteFile.toString())  // Also works with remote specs over HTTP
  outputDir.set("$projectDir/src/main/gen")
  basePackage.set("com.mycompany.myapp.client")
  shouldCleanDir.set(true) // Will delete the outputDir before generating, so be careful!
}
```

Then, if you run `./gradlew skribeGenerate -q` you will see your glorious generated code!