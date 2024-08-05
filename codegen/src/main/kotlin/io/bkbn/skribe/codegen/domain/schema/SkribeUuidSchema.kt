package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.bkbn.skribe.codegen.domain.SkribeSpec

data class SkribeUuidSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeScalarSchema, SerializableSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = ClassName("com.benasher44.uuid", "Uuid")

  override val serializerTypeName: TypeName = ClassName(utilPackage, "UuidSerializer")
}
