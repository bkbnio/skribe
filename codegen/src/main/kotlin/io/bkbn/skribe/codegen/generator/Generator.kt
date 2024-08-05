package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.FileSpec
import io.bkbn.skribe.codegen.domain.SkribeSpec

sealed interface Generator {

  context(SkribeSpec)
  fun generate(): List<FileSpec>
}
