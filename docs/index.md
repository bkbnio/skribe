Skribe is the Yin to [Kompendium's](https://bkbn.gitbook.io/kompendium) Yang.  It is a Gradle plugin for 
generating Ktor API Client boilerplate from an OpenAPI specification.  

Unlike other OpenAPI generators, Skribe does not generate an entire app.  Instead, it relies on Kotlin extension
functions to provide type-safe generated functions for each API call.  This allows you to use the generated code
in a highly flexible manner, taking the routes that you wish to leverage, without having to translate them from 
the definition yourself.

## Usage

