plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("io.bkbn.sourdough.application.jvm")
  id("application")
  id("org.jetbrains.kotlinx.kover")
  id("io.bkbn.skribe") version "0.3.3-SNAPSHOT"
}

dependencies {
  val ktorVersion: String by project
  val kotlinxSerializationVersion: String by project
  val uuidVersion: String by project

  implementation(projects.skribeCodegen)
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("com.benasher44:uuid:$uuidVersion")
}

sourceSets {
  main {
    kotlin {
      srcDir("src/main/gen")
    }
  }
}

skribe {
  specUrl.set(file("../codegen/src/test/resources/factset-prices.yml").absoluteFile.toString())
  outputDir.set("$projectDir/src/main/gen")
  basePackage.set("io.bkbn.sourdough.clients")
  shouldCleanDir.set(true)
}
