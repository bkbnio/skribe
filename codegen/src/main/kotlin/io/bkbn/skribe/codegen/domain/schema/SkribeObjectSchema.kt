package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import io.bkbn.skribe.codegen.utils.StringUtils.convertToCamelCase
import io.bkbn.skribe.codegen.utils.StringUtils.convertToPascalCase

data class SkribeObjectSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val properties: Map<PropertyName, SkribeSchema>,
  val required: List<String>,
  val modelPackage: String,
) : SkribeSchema {
  private val objectName = SkribeObjectName(name)
  fun addressableName(): String = objectName.value.convertToPascalCase()

  override fun toKotlinTypeName(): ClassName = ClassName(modelPackage, addressableName())

  @JvmInline
  private value class SkribeObjectName(val value: String)

  @JvmInline
  value class PropertyName(val value: String) {
    fun addressableName(): String = value.convertToCamelCase()
    fun requiresSerialization(): Boolean = addressableName() != value
  }
}
