package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

sealed interface SkribeSchema {
  val name: String
  val requiresSerialization: Boolean
  fun toKotlinTypeName(): ClassName
}

sealed interface SerializableSchema {
  val serializerClassName: ClassName
}

data class SkribeComposedSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = String::class.asClassName()
}

data class SkribeStringSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = String::class.asClassName()
}

data class SkribeArraySchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = List::class.asClassName()
}

data class SkribeUuidSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  val utilPackage: String,
) : SkribeSchema, SerializableSchema {
  override fun toKotlinTypeName(): ClassName = ClassName("com.benasher44.uuid", "Uuid")
  override val serializerClassName: ClassName = ClassName(utilPackage, "UuidSerializer")
}

data class SkribeReferenceSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val ref: String,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = String::class.asClassName()
}

data class SkribeDateSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = LocalDate::class.asClassName()
}

data class SkribeDateTimeSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = Instant::class.asClassName()
}

data class SkribeBooleanSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = Boolean::class.asClassName()
}

data class SkribeIntegerSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = Int::class.asClassName()
}

data class SkribeEmailSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = String::class.asClassName()
}

data class SkribeNumberSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): ClassName = Number::class.asClassName()
}
