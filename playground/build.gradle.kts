plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("io.bkbn.sourdough.application.jvm")
  id("application")
  id("org.jetbrains.kotlinx.kover")
  //id("io.bkbn.skribe") version "SET_TO_VERSION_UNDER_TEST"
}

dependencies {
  implementation("io.ktor:ktor-client-core:2.3.0")
  implementation("io.ktor:ktor-client-cio:2.3.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

sourceSets {
  main {
    kotlin {
      srcDir("src/main/gen")
    }
  }
}

//skribe {
//  specUrl.set("https://dfv3qgd2ykmrx.cloudfront.net/api_spec/release/v2.json")
//  outputDir.set("$projectDir/src/main/gen")
//  basePackage.set("io.bkbn.sourdough.clients")
//  shouldCleanDir.set(true)
//}
