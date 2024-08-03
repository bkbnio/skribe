package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribePath
import io.swagger.v3.oas.models.parameters.RequestBody

data object RequestBodyConverter : Converter<RequestBody, SkribePath.RequestBody> {
  context(ConverterMetadata)
  override fun convert(input: RequestBody): SkribePath.RequestBody = SkribePath.RequestBody(
    required = input.required ?: false,
    schema = SchemaConverter.convert(mapOf("blah" to input.content.values.first().schema)).first(),
  )
}
