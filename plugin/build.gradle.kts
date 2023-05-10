plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.gradle.plugin-publish")
  id("java-gradle-plugin")
  id("maven-publish")
}

dependencies {
  // Versions
  val kotlinxSerializationVersion: String by project
  val kotlinPoetVersion: String by project

  // Dependencies
  implementation(projects.skribeCodegen)
  implementation("com.squareup:kotlinpoet:${kotlinPoetVersion}")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
}

testing {
  suites {
    named("test", JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        val kotestVersion = "5.6.0"
        implementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")

        implementation(gradleTestKit())
      }
    }
  }
}


gradlePlugin {
  website.set("https://github.com/bkbnio")
  vcsUrl.set("https://github.com/bkbnio/skribe")
  plugins {
    create("Skribe") {
      id = "io.bkbn.skribe"
      displayName = "Skribe API Client Generator"
      description = "Gradle plugin for generating rich API clients from a variety of spec formats"
      implementationClass = "io.bkbn.skribe.plugin.SkribePlugin"
      tags.set(listOf("openapi", "codegen", "ktor"))
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
