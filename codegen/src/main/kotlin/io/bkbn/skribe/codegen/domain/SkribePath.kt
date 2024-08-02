package io.bkbn.skribe.codegen.domain

data class SkribePath(
  val path: String,
  val operation: Operation,
) {
  enum class Operation {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE
  }
}
