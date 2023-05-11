package io.bkbn.skribe.codegen

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema
import kotlinx.serialization.Serializable

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
