package io.bkbn.skribe.plugin

import org.gradle.api.provider.Property

interface SkribeExtension {
  val specUrl: Property<String>
  val basePackage: Property<String>
  val outputDir: Property<String>
  val shouldCleanDir: Property<Boolean>
}
