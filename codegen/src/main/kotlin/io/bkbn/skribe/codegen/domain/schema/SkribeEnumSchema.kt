package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import io.bkbn.skribe.codegen.utils.StringUtils.convertToPascalCase

data class SkribeEnumSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val values: List<SkribeEnumValue>,
  val modelPackage: String,
) : SkribeSchema {
  private val enumName = SkribeEnumName(name)
  fun addressableName(): String = enumName.value.convertToPascalCase()

  override fun toKotlinTypeName(): ClassName = ClassName(modelPackage, addressableName())

  @JvmInline
  private value class SkribeEnumName(val value: String)

  @JvmInline
  value class SkribeEnumValue(val value: String) {
    fun addressableName(): String = value.convertToPascalCase()
    fun requiresSerialization(): Boolean = addressableName() != value
  }
}
