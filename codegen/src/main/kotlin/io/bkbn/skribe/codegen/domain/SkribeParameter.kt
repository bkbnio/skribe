package io.bkbn.skribe.codegen.domain

import io.bkbn.skribe.codegen.utils.StringUtils.convertToCamelCase

sealed interface SkribeParameter {
  enum class In {
    HEADER,
    QUERY,
    PATH,
  }
}

/**
 * A parameter that is not a reference to another parameter
 *
 * @property name The name of the parameter, if it has one
 * @property componentName The name of the parameter in the component dictionary
 * @property description The description of the parameter
 * @property `in` The location of the parameter
 * @property required Whether the parameter is required
 * @property deprecated Whether the parameter is deprecated
 */
data class SkribeParameterLiteral(
  val name: ParameterName?,
  val componentName: String,
  val description: String?,
  val `in`: SkribeParameter.In,
  val required: Boolean,
  val deprecated: Boolean,
) : SkribeParameter {
  @JvmInline
  value class ParameterName(val value: String) {
    fun addressableName(): String = value.convertToCamelCase()
    fun requiresSerialization(): Boolean = addressableName() != value
  }
}

data class SkribeParameterReference(
  val ref: String,
) : SkribeParameter
