package io.bkbn.skribe.codegen.domain.schema

sealed interface SkribePotentiallyScalarSchema : SkribeSchema {
  fun isScalar(): Boolean
}
