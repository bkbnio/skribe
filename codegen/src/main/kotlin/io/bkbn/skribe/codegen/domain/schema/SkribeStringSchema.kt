package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.bkbn.skribe.codegen.domain.SkribeSpec

data class SkribeStringSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeScalarSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}
