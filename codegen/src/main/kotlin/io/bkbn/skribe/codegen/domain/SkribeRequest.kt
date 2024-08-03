package io.bkbn.skribe.codegen.domain

import io.bkbn.skribe.codegen.domain.schema.SkribeSchema

data class SkribeRequest(
  val required: Boolean,
  val schema: SkribeSchema,
)
