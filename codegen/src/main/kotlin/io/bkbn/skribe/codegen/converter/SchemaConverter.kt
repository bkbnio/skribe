package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.schema.SkribeArraySchema
import io.bkbn.skribe.codegen.domain.schema.SkribeBooleanSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeComposedSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeDateSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeDateTimeSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeEmailSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeEnumSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeIntegerSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeNumberSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeObjectSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeReferenceSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeStringSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeUuidSchema
import io.bkbn.skribe.codegen.utils.StringUtils.convertToPascalCase
import io.bkbn.skribe.codegen.utils.StringUtils.getRefKey
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.DateSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.EmailSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema

data object SchemaConverter : Converter<Map<String, Schema<*>>, List<SkribeSchema>> {

  context(ConverterMetadata)
  override fun convert(
    input: Map<String, Schema<*>>,
  ): List<SkribeSchema> = input.map { (name, schema) ->
    when (schema) {
      is ArraySchema -> schema.toSkribeArraySchema(name)
      is BooleanSchema -> schema.toSkribeBooleanSchema(name)
      is ComposedSchema -> schema.toSkribeComposedSchema(name)
      is ObjectSchema -> schema.toSkribeObjectSchema(name)
      is IntegerSchema -> schema.toSkribeIntegerSchema(name)
      is DateSchema -> schema.toSkribeDateSchema(name)
      is DateTimeSchema -> schema.toSkribeDateTimeSchema(name)
      is NumberSchema -> schema.toSkribeNumberSchema(name)
      is UUIDSchema -> schema.toSkribeUuidSchema(name)
      is EmailSchema -> schema.toSkribeEmailSchema(name)
      is StringSchema -> {
        if (schema.enum != null) return@map schema.toSkribeEnumSchema(name)

        schema.toSkribeStringSchema(name)
      }

      else -> when {
        schema.`$ref` != null -> schema.toSkribeReferenceSchema(name)
        else -> error("Unknown schema type: $schema")
      }
    }
  }

  context(ConverterMetadata)
  private fun ObjectSchema.toSkribeObjectSchema(name: String): SkribeObjectSchema = SkribeObjectSchema(
    name = name,
    required = required ?: emptyList(),
    // TODO: What to generate in case of null properties?
    properties = properties?.let {
      val updatedMetadata = ConverterMetadata(
        rootPackage = rootPackage,
        currentPackage = name.convertToPascalCase() // TODO: Use addressable name
      )
      with(updatedMetadata) { convert(it).associateBy { s -> SkribeObjectSchema.PropertyName(s.name) } }
    } ?: emptyMap(),
    modelPackage = currentPackage,
  )

  context(ConverterMetadata)
  private fun StringSchema.toSkribeEnumSchema(name: String): SkribeEnumSchema = SkribeEnumSchema(
    name = name,
    values = enum?.map { SkribeEnumSchema.SkribeEnumValue(it) } ?: error("Schema $name is not an enum type."),
    modelPackage = currentPackage
  )

  private fun ComposedSchema.toSkribeComposedSchema(name: String): SkribeComposedSchema = SkribeComposedSchema(
    name = name,
  )

  private fun StringSchema.toSkribeStringSchema(name: String): SkribeStringSchema = SkribeStringSchema(
    name = name,
  )

  private fun ArraySchema.toSkribeArraySchema(name: String): SkribeArraySchema = SkribeArraySchema(
    name = name,
  )

  context(ConverterMetadata)
  private fun UUIDSchema.toSkribeUuidSchema(name: String): SkribeUuidSchema = SkribeUuidSchema(
    name = name,
    utilPackage = rootPackage.plus(".util")
  )

  private fun Schema<*>.toSkribeReferenceSchema(name: String): SkribeReferenceSchema = SkribeReferenceSchema(
    name = name,
    ref = this.`$ref`.getRefKey()
  )

  private fun DateSchema.toSkribeDateSchema(name: String): SkribeDateSchema = SkribeDateSchema(
    name = name,
  )

  private fun DateTimeSchema.toSkribeDateTimeSchema(name: String): SkribeDateTimeSchema = SkribeDateTimeSchema(
    name = name,
  )

  private fun BooleanSchema.toSkribeBooleanSchema(name: String): SkribeBooleanSchema = SkribeBooleanSchema(
    name = name,
  )

  private fun IntegerSchema.toSkribeIntegerSchema(name: String): SkribeIntegerSchema = SkribeIntegerSchema(
    name = name,
  )

  private fun EmailSchema.toSkribeEmailSchema(name: String): SkribeEmailSchema = SkribeEmailSchema(
    name = name,
  )

  private fun NumberSchema.toSkribeNumberSchema(name: String): SkribeNumberSchema = SkribeNumberSchema(
    name = name,
  )
}
