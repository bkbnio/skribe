package io.bkbn.skribe.codegen.converter

sealed interface Converter<in T, out R> {
  context(ConverterMetadata)
  fun convert(input: T): R
}
