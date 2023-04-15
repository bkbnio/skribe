plugins {
  id("com.github.jakemarsden.git-hooks") version "0.0.2"
  id("io.gitlab.arturbosch.detekt") version "1.22.0"
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
}

detekt {
  config = files("${rootProject.projectDir}/detekt.yml")
  buildUponDefaultConfig = true
}

gitHooks {
  setHooks(
    mapOf(
      "pre-commit" to "detekt",
      "pre-push" to "test"
    )
  )
}
