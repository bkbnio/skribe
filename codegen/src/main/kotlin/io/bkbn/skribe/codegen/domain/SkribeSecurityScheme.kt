package io.bkbn.skribe.codegen.domain

sealed interface SkribeSecurityScheme {
  val type: Type

  enum class Type {
    APIKEY,
    HTTP,
    OAUTH2,
    OPENIDCONNECT,
    MUTUALTLS
  }
}

data class SkribeHttpSecurityScheme(
  val scheme: String,
) : SkribeSecurityScheme {
  override val type: SkribeSecurityScheme.Type = SkribeSecurityScheme.Type.HTTP
}

data class SkribeApiKeySecurityScheme(
  val keyLocation: KeyLocation,
  val name: String,
) : SkribeSecurityScheme {
  override val type: SkribeSecurityScheme.Type = SkribeSecurityScheme.Type.APIKEY

  enum class KeyLocation {
    HEADER,
    QUERY,
    COOKIE,
    PATH,
  }
}

data class SkribeOAuth2SecurityScheme(
  val flows: SkribeOAuth2Flows,
  val authorizationUrl: String,
  val tokenUrl: String,
) : SkribeSecurityScheme {
  override val type: SkribeSecurityScheme.Type = SkribeSecurityScheme.Type.OAUTH2

  data class SkribeOAuth2Flows(
    val authorizationCode: SkribeOAuth2AuthorizationCodeFlow,
    val clientCredentials: SkribeOAuth2ClientCredentialsFlow,
    val implicit: SkribeOAuth2ImplicitFlow,
    val password: SkribeOAuth2PasswordFlow,
    val refreshToken: SkribeOAuth2RefreshTokenFlow,
  )

  data class SkribeOAuth2AuthorizationCodeFlow(
    val authorizationUrl: String,
    val tokenUrl: String,
    val refreshUrl: String,
  )

  data class SkribeOAuth2ClientCredentialsFlow(
    val tokenUrl: String,
  )

  data class SkribeOAuth2ImplicitFlow(
    val authorizationUrl: String,
    val refreshUrl: String,
  )

  data class SkribeOAuth2PasswordFlow(
    val tokenUrl: String,
    val refreshUrl: String,

  )

  data class SkribeOAuth2RefreshTokenFlow(
    // TODO: What is this?
    val refreshUrl: String,
  )
}
