package io.bkbn.skribe.codegen.utils

import java.util.Locale

object StringUtils {
  fun String.capitalized() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
  }

  fun String.getRefKey() = split("/").last().trim()

  fun String.convertToCamelCase(): String = sanitize().let { sanitized ->
    val words = splitIntoWords(sanitized)
    words.mapIndexed { index, word ->
      when {
        index == 0 -> word.lowercase()
        word.isAcronym() -> word
        else -> word.capitalized()
      }
    }.joinToString("")
  }

  fun String.convertToPascalCase(): String = sanitize().let { sanitized ->
    val words = splitIntoWords(sanitized)
    words.joinToString("") { word ->
      when {
        word.isAcronym() -> word
        else -> word.capitalized()
      }
    }
  }

  private fun splitIntoWords(str: String): List<String> = when {
    str.isSnake() -> str.split("_")
    str.isCamelCase() || str.isPascalCase() -> str.split(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
    else -> listOf(str)
  }

  private fun String.isAcronym() = length > 1 && all { it.isUpperCase() }
  private fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))
  private fun String.isCamelCase() = matches(Regex("^[a-z]+([A-Z][a-z0-9]+)*$"))
  private fun String.isPascalCase() = matches(Regex("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$"))
  private fun String.sanitize(): String = trim().replace(Regex("[\\s.-]+"), "_")

}
