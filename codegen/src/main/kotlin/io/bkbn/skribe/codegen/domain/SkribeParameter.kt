package io.bkbn.skribe.codegen.domain

data class SkribeParameter(
  val name: String,
  val description: String?,
  val `in`: In,
  val required: Boolean,
  val deprecated: Boolean,
) {
  enum class In {
    HEADER,
    QUERY,
    PATH,
  }
}
