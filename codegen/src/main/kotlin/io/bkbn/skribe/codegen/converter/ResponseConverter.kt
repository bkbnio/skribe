package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeResponse
import io.swagger.v3.oas.models.responses.ApiResponse

data object ResponseConverter : Converter<Map<String, ApiResponse>, List<SkribeResponse>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, ApiResponse>): List<SkribeResponse> = input.map { (name, response) ->
    SkribeResponse(
      name = name.toIntOrNull()?.let { name },
      description = response.description,
      schema = response.content?.let { SchemaConverter.convert(mapOf("blah" to it.values.first().schema)).first() },
      statusCode = name.toIntOrNull(),
    )
  }
}
