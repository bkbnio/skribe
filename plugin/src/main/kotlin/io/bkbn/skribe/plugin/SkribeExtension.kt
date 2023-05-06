package io.bkbn.skribe.plugin

import org.gradle.api.provider.Property

interface SkribeExtension {
  val specUrl: Property<String>
  val outputDir: Property<String>
}
