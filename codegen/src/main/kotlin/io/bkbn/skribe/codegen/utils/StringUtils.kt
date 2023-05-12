package io.bkbn.skribe.codegen.utils

import java.util.Locale

object StringUtils {
  fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))

  fun String.sanitizeEnumConstant(): String =
    trim().replace(Regex("[\\s-/]+"), "_").uppercase(Locale.getDefault())

  fun String.sanitizePropertyName(): String = trim().replace(Regex("[\\s.-]+"), "_").lowercase(Locale.getDefault())
  fun String.formattedParamName(): String = this.sanitizePropertyName().snakeToCamel()

  fun String.getRefKey() = split("/").last()

  fun String.formatPropertyName(): String = sanitizePropertyName().let {
    when {
      it.isSnake() -> it.snakeToCamel()
      else -> it
    }
  }
}
