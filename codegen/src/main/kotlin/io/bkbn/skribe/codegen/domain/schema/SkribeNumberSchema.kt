package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.bkbn.skribe.codegen.domain.SkribeSpec

data class SkribeNumberSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeScalarSchema, SerializableSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = Number::class.asClassName()
  override val serializerTypeName: TypeName = ClassName(utilPackage, "NumberSerializer")
}
