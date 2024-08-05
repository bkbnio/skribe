package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribeApiKeySecurityScheme
import io.bkbn.skribe.codegen.domain.SkribeHttpSecurityScheme
import io.bkbn.skribe.codegen.domain.SkribeOAuth2SecurityScheme
import io.bkbn.skribe.codegen.domain.SkribeSecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme

data object SecuritySchemeConverter : Converter<Map<String, SecurityScheme>, List<SkribeSecurityScheme>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, SecurityScheme>): List<SkribeSecurityScheme> = input.map { (name, scheme) ->
    when (scheme.type) {
      SecurityScheme.Type.APIKEY -> SkribeApiKeySecurityScheme(
        keyLocation = when (scheme.`in`) {
          SecurityScheme.In.HEADER -> SkribeApiKeySecurityScheme.KeyLocation.HEADER
          SecurityScheme.In.QUERY -> SkribeApiKeySecurityScheme.KeyLocation.QUERY
          SecurityScheme.In.COOKIE -> SkribeApiKeySecurityScheme.KeyLocation.COOKIE
          else -> error("Unknown key location: ${scheme.`in`}")
        },
        name = name,
      )

      SecurityScheme.Type.HTTP -> SkribeHttpSecurityScheme(
        scheme = scheme.scheme,
      )

      SecurityScheme.Type.OAUTH2 -> SkribeOAuth2SecurityScheme(
        flows = SkribeOAuth2SecurityScheme.SkribeOAuth2Flows(
          authorizationCode = SkribeOAuth2SecurityScheme.SkribeOAuth2AuthorizationCodeFlow(
            authorizationUrl = scheme.flows?.authorizationCode?.authorizationUrl ?: "todo find auth url",
            tokenUrl = scheme.flows?.authorizationCode?.tokenUrl ?: "todo find token url",
            refreshUrl = scheme.flows?.authorizationCode?.refreshUrl ?: "todo find refresh url",
          ),
          clientCredentials = SkribeOAuth2SecurityScheme.SkribeOAuth2ClientCredentialsFlow(
            tokenUrl = scheme.flows?.clientCredentials?.tokenUrl ?: "todo find token url",
          ),
          implicit = SkribeOAuth2SecurityScheme.SkribeOAuth2ImplicitFlow(authorizationUrl = scheme.flows?.implicit?.authorizationUrl ?: "todo find auth url",
            refreshUrl = scheme.flows?.implicit?.refreshUrl ?: "todo find refresh url",
          ),
          password = SkribeOAuth2SecurityScheme.SkribeOAuth2PasswordFlow(
            tokenUrl = scheme.flows?.password?.tokenUrl ?: "todo find token url",
            refreshUrl = scheme.flows?.password?.refreshUrl ?: "todo find refresh url",
          ),
          refreshToken = SkribeOAuth2SecurityScheme.SkribeOAuth2RefreshTokenFlow(
            refreshUrl = "todo find refresh token",
          ),
        ),
        authorizationUrl = scheme.flows?.authorizationCode?.authorizationUrl ?: "todo find auth url",
        tokenUrl = scheme.flows?.authorizationCode?.tokenUrl ?: "todo find token url",
      )

      SecurityScheme.Type.OPENIDCONNECT -> TODO()
      SecurityScheme.Type.MUTUALTLS -> TODO()
      else -> error("Unknown security scheme type: ${scheme.`in`}")
    }
//    SkribeSecurityScheme(
//      type = when (scheme.type) {
//        SecurityScheme.Type.APIKEY -> SkribeSecurityScheme.Type.APIKEY
//        SecurityScheme.Type.HTTP -> SkribeSecurityScheme.Type.HTTP
//        SecurityScheme.Type.OAUTH2 -> SkribeSecurityScheme.Type.OAUTH2
//        SecurityScheme.Type.OPENIDCONNECT -> SkribeSecurityScheme.Type.OPENIDCONNECT
//        SecurityScheme.Type.MUTUALTLS -> SkribeSecurityScheme.Type.MUTUALTLS
//        else -> error("Unknown security scheme type: ${scheme.type}")
//      },
//      scheme = scheme.scheme,
//    )
  }
}
