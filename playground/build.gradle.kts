plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("io.bkbn.sourdough.application.jvm")
  id("application")
  id("org.jetbrains.kotlinx.kover")
  id("io.bkbn.skribe") version "latest.integration"
}

dependencies {
  val ktorVersion: String by project
  val kotlinxSerializationVersion: String by project
  val uuidVersion: String by project

  implementation(projects.skribeCodegen)
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
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
  shouldCleanDir = true
  outputDir = "$projectDir/src/main/gen"

  api {
    basePackage = "com.alpaca.client.broker"
    specUrl = file("../codegen/src/test/resources/alpaca-broker.yml").absoluteFile.toString()
  }

  api {
    basePackage = "com.factset.client.prices"
    specUrl = file("../codegen/src/test/resources/factset-prices.yml").absoluteFile.toString()
  }

  api {
    basePackage = "com.factset.client.fundamentals"
    specUrl = file("../codegen/src/test/resources/factset-fundamentals.yml").absoluteFile.toString()
  }
}
