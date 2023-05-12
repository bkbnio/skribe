package io.bkbn.skribe.codegen.utils

import java.util.Locale

object StringUtils {
  fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  private fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))
  private fun String.isCamelCase() = matches(Regex("^[a-z]+([A-Z][a-z0-9]+)+$"))
  private fun String.isPascalCase() = matches(Regex("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$"))

  fun String.sanitizeEnumConstant(): String =
    trim().replace(Regex("[\\s-/]+"), "_").uppercase(Locale.getDefault())

  fun String.sanitizePropertyName(): String = trim().replace(Regex("[\\s.-]+"), "_")

  fun String.getRefKey() = split("/").last()

  fun String.convertToCamelCase(): String = sanitizePropertyName().let {
    when {
      it.isSnake() -> it.lowercase().snakeToCamel()
      it.isCamelCase() -> it
      it.isPascalCase() -> it.replaceFirstChar { char -> char.lowercase() }
      else -> it.lowercase()
    }
  }

  fun String.convertToPascalCase(): String = sanitizePropertyName().let {
    when {
      it.isSnake() -> it.lowercase().snakeToCamel().replaceFirstChar { char -> char.uppercase() }
      it.isCamelCase() -> it.replaceFirstChar { char -> char.uppercase() }
      it.isPascalCase() -> it
      else -> it.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
  }
}
