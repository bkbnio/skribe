package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeSecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme

data object SecuritySchemeConverter : Converter<Map<String, SecurityScheme>, List<SkribeSecurityScheme>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, SecurityScheme>): List<SkribeSecurityScheme> = input.map { (name, scheme) ->
    SkribeSecurityScheme(
      type = when (scheme.type) {
        SecurityScheme.Type.APIKEY -> SkribeSecurityScheme.Type.APIKEY
        SecurityScheme.Type.HTTP -> SkribeSecurityScheme.Type.HTTP
        SecurityScheme.Type.OAUTH2 -> SkribeSecurityScheme.Type.OAUTH2
        SecurityScheme.Type.OPENIDCONNECT -> SkribeSecurityScheme.Type.OPENIDCONNECT
        SecurityScheme.Type.MUTUALTLS -> SkribeSecurityScheme.Type.MUTUALTLS
        else -> error("Unknown security scheme type: ${scheme.type}")
      },
      scheme = scheme.scheme,
    )
  }
}
