import org.gradle.kotlin.dsl.detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("io.bkbn.sourdough.library.jvm")
  id("io.gitlab.arturbosch.detekt")
  id("com.adarshr.test-logger")
  id("org.jetbrains.kotlinx.kover")
  id("maven-publish")
  id("java-library")
  id("signing")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
  }
}

detekt {
  autoCorrect = true
}

dependencies {
  // Versions
  val detektVersion: String by project
  val ktorVersion: String by project
  val kotlinPoetVersion: String by project
  val swaggerParserVersion: String by project
  val kotlinxSerializationVersion: String by project
  val kotlinxDatetimeVersion: String by project
  val uuidVersion: String by project

  implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
  implementation("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("com.benasher44:uuid:$uuidVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")

  implementation("io.ktor:ktor-client-core:$ktorVersion")

  // Formatting
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
}

sourdoughLibrary {
  libraryName.set("Skribe Codegen")
  libraryDescription.set("Generates KotlinPoet Manifests for creating HTTP Clients from an OpenAPI Spec")
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter()
      dependencies {
        // Kotest
        val kotestVersion: String by project
        val kotestCompileTestingAssertionsVersion: String by project
        val kotlinxSerializationVersion: String by project
        val kotlinCompileTestingVersion: String by project
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
        implementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
        implementation("io.kotest.extensions:kotest-assertions-compiler:$kotestCompileTestingAssertionsVersion")
        implementation("com.github.tschuchortdev:kotlin-compile-testing:$kotlinCompileTestingVersion")
      }
    }
  }
}
