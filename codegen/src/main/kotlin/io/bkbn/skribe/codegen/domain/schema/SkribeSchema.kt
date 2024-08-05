package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.TypeName
import io.bkbn.skribe.codegen.domain.SkribeSpec

sealed interface SkribeSchema {
  // TODO: Value class?
  val name: String
  val requiresSerialization: Boolean

  context(SkribeSpec)
  fun toKotlinTypeName(): TypeName
}
