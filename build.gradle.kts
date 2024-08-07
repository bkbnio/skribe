plugins {
  // Root Plugins
  id("io.bkbn.sourdough.root") version "0.12.0"
  id("com.github.jakemarsden.git-hooks") version "0.0.2"
  id("org.jetbrains.kotlinx.kover") version "0.6.1"
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"

  // Child Plugins
  kotlin("jvm") version "2.0.0" apply false
  kotlin("plugin.serialization") version "2.0.0" apply false
  id("com.gradle.plugin-publish") version "1.2.0" apply false
  id("io.bkbn.sourdough.library.jvm") version "0.12.0" apply false
  id("io.bkbn.sourdough.application.jvm") version "0.12.0" apply false
  id("io.gitlab.arturbosch.detekt") version "1.22.0" apply false
  id("com.adarshr.test-logger") version "3.2.0" apply false
}

allprojects {
  group = "io.bkbn"

  version = run {
    val baseVersion =
      project.findProperty("project.version") ?: error("project.version needs to be set in gradle.properties")
    when ((project.findProperty("release") as? String)?.toBoolean()) {
      true -> baseVersion
      else -> "$baseVersion-SNAPSHOT"
    }
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }

  plugins.withType(io.bkbn.sourdough.gradle.library.jvm.LibraryJvmPlugin::class) {
    extensions.configure(io.bkbn.sourdough.gradle.library.jvm.LibraryJvmExtension::class) {
      githubOrg.set("bkbnio")
      githubRepo.set("skribe")
      licenseName.set("MIT License")
      licenseUrl.set("https://mit-license.org")
      developerId.set("unredundant")
      developerName.set("Ryan Brink")
      developerEmail.set("admin@bkbn.io")
    }
  }
}

gitHooks {
  setHooks(
    mapOf(
      "pre-commit" to "detekt",
      "pre-push" to "test"
    )
  )
}
