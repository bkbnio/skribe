package io.bkbn.skribe.codegen.generator

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.bkbn.skribe.codegen.domain.schema.SkribeBooleanSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeDateSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeDateTimeSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeEmailSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeIntegerSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeNumberSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeScalarSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeStringSchema
import io.bkbn.skribe.codegen.domain.schema.SkribeUuidSchema
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data object TypeAliasGenerator : Generator {
  context(SkribeSpec)
  override fun generate(): List<FileSpec> =
    schemas.filterIsInstance<SkribeScalarSchema>().map { schema ->
      when (schema) {
        is SkribeBooleanSchema -> createTypeAlias(schema, Boolean::class.asTypeName())
        is SkribeDateSchema -> createTypeAlias(schema, LocalDate::class.asTypeName())
        is SkribeDateTimeSchema -> createTypeAlias(schema, Instant::class.asTypeName())
        is SkribeEmailSchema -> createTypeAlias(schema, String::class.asTypeName())
        is SkribeIntegerSchema -> createTypeAlias(schema, Int::class.asTypeName())
        is SkribeNumberSchema -> createTypeAlias(schema, Number::class.asTypeName())
        is SkribeStringSchema -> createTypeAlias(schema, String::class.asTypeName())
        is SkribeUuidSchema -> createTypeAlias(schema, Uuid::class.asTypeName())
      }
    }

  context(SkribeSpec)
  private fun createTypeAlias(schema: SkribeSchema, typeName: TypeName): FileSpec =
    FileSpec.builder(modelPackage, schema.name).apply {
      addTypeAlias(TypeAliasSpec.builder(schema.name, typeName).build())
    }.build()
}
