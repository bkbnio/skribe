package io.bkbn.skribe.codegen.domain

import io.bkbn.skribe.codegen.utils.StringUtils.convertToCamelCase
import io.bkbn.skribe.codegen.utils.StringUtils.convertToPascalCase

data class SkribePath(
  val path: String,
  val name: PathName,
  val description: String?,
  val operation: Operation,
  val requestBody: SkribeRequest?,
  val responses: List<SkribeResponse>,
) {

  @JvmInline
  value class PathName(val value: String) {
    fun fileName(): String = value.convertToPascalCase()
    fun addressableName(): String = value.convertToCamelCase()
  }

  enum class Operation {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE
  }
}
