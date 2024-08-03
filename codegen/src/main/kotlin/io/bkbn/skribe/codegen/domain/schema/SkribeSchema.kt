package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.bkbn.skribe.codegen.utils.StringUtils.getRefKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

sealed interface SkribeSchema {
  val name: String
  val requiresSerialization: Boolean

  context(SkribeSpec)
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
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeStringSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeArraySchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val items: SkribeSchema,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = List::class.asClassName().parameterizedBy(
    when (items.requiresSerialization) {
      true -> items.toKotlinTypeName().copy(annotations = listOf(AnnotationSpec.builder(Serializable::class).apply {
        addMember("with = %T::class", (items as SerializableSchema).serializerTypeName)
      }.build()))

      false -> items.toKotlinTypeName()
    }
  )
}

data class SkribeUuidSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeSchema, SerializableSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = ClassName("com.benasher44.uuid", "Uuid")
  override val serializerTypeName: TypeName = ClassName(utilPackage, "UuidSerializer")
}

data class SkribeReferenceSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val ref: String,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = ClassName(modelPackage, ref.getRefKey())
}

data class SkribeDateSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = LocalDate::class.asClassName()
}

data class SkribeDateTimeSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = Instant::class.asClassName()
}

data class SkribeBooleanSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = Boolean::class.asClassName()
}

data class SkribeIntegerSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = Int::class.asClassName()
}

data class SkribeEmailSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = String::class.asClassName()
}

data class SkribeNumberSchema(
  override val name: String,
  override val requiresSerialization: Boolean = true,
  override val utilPackage: String,
) : SkribeSchema, SerializableSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = Number::class.asClassName()
  override val serializerTypeName: TypeName = ClassName(utilPackage, "NumberSerializer")
}

data class SkribeFreeFormSchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
) : SkribeSchema {
  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = JsonElement::class.asClassName()
}
