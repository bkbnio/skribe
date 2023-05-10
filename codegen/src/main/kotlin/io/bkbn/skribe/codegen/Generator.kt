package io.bkbn.skribe.codegen

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import java.util.Locale

internal sealed interface Generator {
  val basePackage: String
  val openApi: OpenAPI
  val requestPackage
    get() = "$basePackage.requests"
  val modelPackage
    get() = "$basePackage.models"
  val utilPackage
    get() = "$basePackage.util"

  fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))

  fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  fun String.sanitizeEnumConstant(): String =
    trim().replace(Regex("[\\s-]+"), "_").uppercase(Locale.getDefault())

  fun String.sanitizePropertyName(): String = trim().replace(Regex("\\s+"), "_").lowercase(Locale.getDefault())

  val Schema<*>.enumConstants: List<String>
    get() = enum?.map { it.toString() } ?: emptyList()

  fun String.getRefKey() = split("/").last()

  fun Schema<*>.isReferenceSchema(): Boolean = `$ref` != null
}
