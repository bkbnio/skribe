package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeParameter
import io.swagger.v3.oas.models.parameters.Parameter

data object ParameterConverter : Converter<Map<String, Parameter>, List<SkribeParameter>> {
  override fun convert(input: Map<String, Parameter>): List<SkribeParameter> = input.map { (name, parameter) ->
    SkribeParameter(
      name = name,
      description = parameter.description,
      `in` = parameter.`in`.toSkribeParameterIn(),
      required = parameter.required,
      deprecated = parameter.deprecated ?: false,
    )
  }

  private fun String.toSkribeParameterIn(): SkribeParameter.In = when (this) {
    "header" -> SkribeParameter.In.HEADER
    "query" -> SkribeParameter.In.QUERY
    "path" -> SkribeParameter.In.PATH
    else -> error("Unknown parameter type: $this")
  }
}
