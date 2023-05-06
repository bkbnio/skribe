plugins {
  kotlin("jvm") version "1.8.20"
  kotlin("plugin.serialization") version "1.8.20"
  id("com.gradle.plugin-publish") version "1.2.0"
  id("java-gradle-plugin")
  id("maven-publish")
}

dependencies {
  // Versions
  val spektVersion = "0.1.0-SNAPSHOT"
  val ktorVersion = "2.3.0"

  // Dependencies
  implementation("io.bkbn:spekt-openapi-3-0:$spektVersion")
  implementation("io.bkbn:spekt-swagger-2-0:$spektVersion")
  implementation("io.bkbn:spekt-api-client-codegen:$spektVersion")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("com.squareup.okhttp3:okhttp:4.11.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

gradlePlugin {
  plugins {
    create("Skribe") {
      id = "io.bkbn.skribe"
      displayName = "Skribe API Client Generator"
      description = "Gradle plugin for generating rich API clients from a variety of spec formats"
      implementationClass = "io.bkbn.skribe.plugin.SkribePlugin"
    }
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}

publishing {
  repositories {
    maven {
      name = "GithubPackages"
      url = uri("https://maven.pkg.github.com/bkbnio/skribe")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}
