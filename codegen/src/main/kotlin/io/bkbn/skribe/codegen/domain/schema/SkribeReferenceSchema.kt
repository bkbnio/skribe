package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.bkbn.skribe.codegen.utils.StringUtils.convertToPascalCase
import io.bkbn.skribe.codegen.utils.StringUtils.getRefKey

data class SkribeReferenceSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val ref: String,
) : SkribeSchema {
  context(SkribeSpec)
  // TODO: Hacky w/ pascal crap
  override fun toKotlinTypeName(): TypeName = ClassName(modelPackage, ref.getRefKey().convertToPascalCase())
}
