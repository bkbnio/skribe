plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("io.bkbn.sourdough.application.jvm")
  id("application")
  id("org.jetbrains.kotlinx.kover")
  id("io.bkbn.skribe") version "0.1.2-SNAPSHOT"
}

sourceSets {
  main {
    kotlin {
      srcDir("src/main/gen")
    }
  }
}

skribe {
  specUrl.set("https://dfv3qgd2ykmrx.cloudfront.net/api_spec/release/v2.json")
  outputDir.set("$projectDir/src/main/gen")
  basePackage.set("io.bkbn.sourdough.clients")
  shouldCleanDir.set(true)
}
