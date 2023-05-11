package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.oas.models.OpenAPI

class ModelGenerator(override val basePackage: String, override val openApi: OpenAPI) : Generator {
  fun generate(): Map<String, FileSpec> {
    return openApi.generateComponentSchemaModels() +
      openApi.generateComponentResponseModels() +
      openApi.generateComponentRequestBodyModels()
  }

  private fun OpenAPI.generateComponentSchemaModels(): Map<String, FileSpec> =
    components.schemas.mapValues { (name, schema) ->
      FileSpec.builder(modelPackage, name).apply {
        addSchemaType(name, schema)
      }.build()
    }

  private fun OpenAPI.generateComponentResponseModels(): Map<String, FileSpec> =
    components.responses.mapValues { (name, response) ->
      val schema = response.content.values.first().schema
      FileSpec.builder(modelPackage, name).apply {
        addSchemaType(name, schema)
      }.build()
    }

  private fun OpenAPI.generateComponentRequestBodyModels(): Map<String, FileSpec> {
    return emptyMap()
  }
}
