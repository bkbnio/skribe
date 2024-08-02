package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

sealed interface SkribeSchema {
  val name: String
  val requiresSerialization: Boolean
  fun toKotlinTypeName(): TypeName
}

sealed interface SerializableSchema {
  val utilPackage: String
  val serializerTypeName: TypeName
}

data class SkribeComposedSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeStringSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeArraySchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val items: SkribeSchema,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = List::class.asClassName().parameterizedBy(items.toKotlinTypeName())
}

data class SkribeUuidSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeSchema, SerializableSchema {
  override fun toKotlinTypeName(): TypeName = ClassName("com.benasher44.uuid", "Uuid")
  override val serializerTypeName: TypeName = ClassName(utilPackage, "UuidSerializer")
}

data class SkribeReferenceSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val ref: String,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeDateSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = LocalDate::class.asClassName()
}

data class SkribeDateTimeSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = Instant::class.asClassName()
}

data class SkribeBooleanSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = Boolean::class.asClassName()
}

data class SkribeIntegerSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = Int::class.asClassName()
}

data class SkribeEmailSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeNumberSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeSchema, SerializableSchema {
  override fun toKotlinTypeName(): TypeName = Number::class.asClassName()
  override val serializerTypeName: TypeName = ClassName(utilPackage, "NumberSerializer")
}
