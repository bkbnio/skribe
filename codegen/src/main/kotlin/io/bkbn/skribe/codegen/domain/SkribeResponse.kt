package io.bkbn.skribe.codegen.domain

import io.bkbn.skribe.codegen.domain.schema.SkribeSchema

data class SkribeResponse(
  val name: String?,
  val description: String?,
  val schema: SkribeSchema?,
  val statusCode: Int?,
)
