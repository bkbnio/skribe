package io.bkbn.skribe.codegen.converter

data class ConverterMetadata(
  val rootPackage: String,
  val currentPackage: String = rootPackage,
)
