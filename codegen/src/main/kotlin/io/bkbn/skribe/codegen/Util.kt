package io.bkbn.skribe.codegen

import io.swagger.v3.oas.models.media.Schema
import java.util.Locale

object Util {

  internal fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  internal fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))

  internal fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  internal fun String.sanitizeEnumConstant(): String =
    trim().replace(Regex("[\\s-]+"), "_").uppercase(Locale.getDefault())

  internal fun String.sanitizePropertyName(): String = trim().replace(Regex("\\s+"), "_").lowercase(Locale.getDefault())

  internal val Schema<*>.enumConstants: List<String>
    get() = enum?.map { it.toString() } ?: emptyList()

  internal fun String.getRefKey() = split("/").last()

  internal fun Schema<*>.isReferenceSchema(): Boolean = `$ref` != null
}
