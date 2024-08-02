package io.bkbn.skribe.codegen.domain

import io.bkbn.skribe.codegen.domain.schema.SkribeSchema

data class SkribeSpec(
  val rootPackage: String,
  val parameters: List<SkribeParameter>,
  val responses: List<SkribeResponse>,
  val paths: List<SkribePath>,
  val schemas: List<SkribeSchema>,
  val securitySchemes: List<SkribeSecurityScheme>,
)
