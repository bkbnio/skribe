package io.bkbn.skribe.plugin

import org.gradle.api.provider.Property

//interface SkribeExtension {
//  val specUrl: Property<String>
//  val basePackage: Property<String>
//  val outputDir: Property<String>
//  val shouldCleanDir: Property<Boolean>
//}

abstract class SkribeExtension {
  val apis: MutableList<ApiExtension> = mutableListOf()

  var outputDir: String? = null
  var shouldCleanDir: Boolean = false

  fun api(configure: ApiExtension.() -> Unit) {
    apis.add(ApiExtension().apply(configure))
  }
}

class ApiExtension {
  lateinit var specUrl: String
  lateinit var basePackage: String
}
