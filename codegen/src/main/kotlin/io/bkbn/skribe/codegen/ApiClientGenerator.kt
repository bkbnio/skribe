package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.bkbn.spekt.common.Spek
import io.bkbn.spekt.openapi_3_0.OpenApi
import io.bkbn.spekt.swagger_2_0.Swagger

object ApiClientGenerator {
  fun generate(spek: Spek, basePackage: String): List<FileSpec> = when (spek) {
    is OpenApi -> OpenApiClientGenerator(basePackage).generate(spek)
    is Swagger -> TODO()
    else -> throw IllegalArgumentException("Unknown Spek type: ${spek::class.simpleName}")
  }
}
