package io.bkbn.skribe.codegen.domain

// TODO: Sealed interface?
data class SkribeSecurityScheme(
  val type: Type,
  val scheme: String,
) {
  enum class Type {
    APIKEY,
    HTTP,
    OAUTH2,
    OPENIDCONNECT,
    MUTUALTLS
  }
}
