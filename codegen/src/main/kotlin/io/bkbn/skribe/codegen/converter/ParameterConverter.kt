package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeParameter
import io.bkbn.skribe.codegen.domain.SkribeParameterLiteral
import io.bkbn.skribe.codegen.domain.SkribeParameterReference
import io.swagger.v3.oas.models.parameters.Parameter

data object ParameterConverter : Converter<Map<String, Parameter>, List<SkribeParameter>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, Parameter>): List<SkribeParameter> = input.map { (name, parameter) ->
    when (parameter.`$ref`) {
      null -> SkribeParameterLiteral(
        name = parameter.name?.let { SkribeParameterLiteral.ParameterName(it) },
        componentName = name,
        description = parameter.description,
        `in` = parameter.`in`?.toSkribeParameterIn() ?: error("Parameter `$name` has no `in` value"),
        required = parameter.required,
        deprecated = parameter.deprecated ?: false,
      )

      else -> SkribeParameterReference(
        ref = parameter.`$ref`
      )
    }
  }

  private fun String.toSkribeParameterIn(): SkribeParameter.In = when (this) {
    "header" -> SkribeParameter.In.HEADER
    "query" -> SkribeParameter.In.QUERY
    "path" -> SkribeParameter.In.PATH
    else -> error("Unknown parameter type: $this")
  }
}
