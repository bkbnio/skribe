package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.oas.models.OpenAPI

class RequestGenerator(private val basePackage: String) {
  fun generate(spec: OpenAPI): Map<String, FileSpec> {
    return emptyMap()
  }
}
