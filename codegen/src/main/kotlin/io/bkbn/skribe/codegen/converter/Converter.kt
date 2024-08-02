package io.bkbn.skribe.codegen.converter

sealed interface Converter<in T, out R> {
  fun convert(input: T): R
}
