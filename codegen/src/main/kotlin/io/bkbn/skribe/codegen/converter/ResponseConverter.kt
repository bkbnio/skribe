package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeResponse
import io.swagger.v3.oas.models.responses.ApiResponse

data object ResponseConverter : Converter<Map<String, ApiResponse>, List<SkribeResponse>> {
  override fun convert(input: Map<String, ApiResponse>): List<SkribeResponse> = input.map { (name, response) ->
    SkribeResponse(
      name = name,
      description = response.description,
    )
  }
}
