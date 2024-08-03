package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.bkbn.skribe.codegen.domain.schema.SerializableSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeArraySchema
import io.bkbn.skribe.codegen.domain.schema.SkribeEnumSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeObjectSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data object ModelGenerator : Generator {

  context(SkribeSpec)
  override fun generate(): List<FileSpec> =
    schemas.filterIsInstance<SkribeObjectSchema>().map { schema ->
      val modelPackage = "$rootPackage.model"
      FileSpec.builder(modelPackage, schema.addressableName()).apply {
        with(schema) { addType(toModelType()) }
      }.build()
    }

  context(SkribeObjectSchema, SkribeSpec)
  private fun toModelType(): TypeSpec {
    return TypeSpec.classBuilder(addressableName()).apply {
      addModifiers(KModifier.DATA)
      addAnnotation(AnnotationSpec.builder(Serializable::class).build())
      constructModelProperties()
    }.build()
  }

  context(SkribeObjectSchema, SkribeSpec)
  private fun TypeSpec.Builder.constructModelProperties() {
    primaryConstructor(
      FunSpec.constructorBuilder().apply {
        properties.forEach { (name, schema) ->
          addParameter(name.addressableName(), schema.toKotlinTypeName())
        }
      }.build()
    )
    properties.forEach { (name, schema) ->
      addProperty(constructModelProperty(name, schema))
    }

    // TODO: Getting unwieldy, also won't work recursively
    properties.values.filterIsInstance<SkribeObjectSchema>().forEach { schema ->
      with(schema) {
        addType(toModelType())
      }
    }
    properties.values.filterIsInstance<SkribeEnumSchema>().forEach { schema ->
      with(schema) {
        addType(EnumGenerator.toEnumType())
      }
    }
    properties.values.filterIsInstance<SkribeArraySchema>().forEach { schema ->
      if (schema.items is SkribeObjectSchema) {
        with(schema.items) {
          addType(toModelType())
        }
      }
      if (schema.items is SkribeEnumSchema) {
        with(schema.items) {
          addType(EnumGenerator.toEnumType())
        }
      }
    }
  }

  context(SkribeSpec)
  private fun constructModelProperty(name: SkribeObjectSchema.PropertyName, schema: SkribeSchema) =
    PropertySpec.builder(name.addressableName(), schema.toKotlinTypeName()).apply {
      initializer(name.addressableName())
      if (name.requiresSerialization()) {
        addAnnotation(
          AnnotationSpec.builder(SerialName::class).apply {
            addMember("%S", name.value)
          }.build()
        )
      }
      if (schema.requiresSerialization) {
        require(schema is SerializableSchema) { "Schema $schema does not implement SerializableSchema" }
        addAnnotation(
          AnnotationSpec.builder(Serializable::class).apply {
            addMember("with = %T::class", (schema as SerializableSchema).serializerTypeName)
          }.build()
        )
      }
    }.build()
}
