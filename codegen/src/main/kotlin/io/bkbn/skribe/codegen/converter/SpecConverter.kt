package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.swagger.v3.oas.models.OpenAPI

class SpecConverter(basePackage: String) : Converter<OpenAPI, SkribeSpec> {
  private val schemaConverter = SchemaConverter(basePackage)
  override fun convert(input: OpenAPI): SkribeSpec = SkribeSpec(
    parameters = ParameterConverter.convert(input.components.parameters),
    responses = ResponseConverter.convert(input.components.responses),
    paths = PathConverter.convert(input.paths),
    schemas = schemaConverter.convert(input.components.schemas),
    securitySchemes = SecuritySchemeConverter.convert(input.components.securitySchemes),
  )
}
