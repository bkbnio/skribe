package io.bkbn.skribe.codegen.utils

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody

object SchemaUtils {
  val Schema<*>.enumConstants: List<String>
    get() = enum?.map { it.toString() } ?: emptyList()

  fun Schema<*>.isReferenceSchema(): Boolean = `$ref` != null

  val Schema<*>.requiredProperties: List<String>
    get() = required ?: emptyList()

  val Schema<*>.propertiesOrEmpty: Map<String, Schema<*>>
    get() = properties ?: emptyMap()

  val RequestBody.safeRequired: Boolean get() = required ?: false
  val Parameter.safeRequired: Boolean get() = required ?: false
}
