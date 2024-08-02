package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.swagger.v3.oas.models.OpenAPI

data object SpecConverter : Converter<OpenAPI, SkribeSpec> {

  context(ConverterMetadata)
  override fun convert(input: OpenAPI): SkribeSpec = SkribeSpec(
    rootPackage = rootPackage,
    parameters = ParameterConverter.convert(input.components.parameters),
    responses = ResponseConverter.convert(input.components.responses),
    paths = PathConverter.convert(input.paths),
    schemas = SchemaConverter.convert(input.components.schemas),
    securitySchemes = SecuritySchemeConverter.convert(input.components.securitySchemes),
  )
}
