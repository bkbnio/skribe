package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeRequest
import io.swagger.v3.oas.models.parameters.RequestBody

data object RequestBodyConverter : Converter<RequestBody, SkribeRequest> {
  context(ConverterMetadata)
  override fun convert(input: RequestBody): SkribeRequest = SkribeRequest(
    required = input.required ?: false,
    schema = SchemaConverter.convert(mapOf("blah" to input.content.values.first().schema)).first(),
  )
}
