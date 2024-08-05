package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.bkbn.skribe.codegen.domain.SkribeSpec
import kotlinx.serialization.Serializable

data class SkribeArraySchema(
  override val name: String,
  override val requiresSerialization: Boolean = false,
  val items: SkribeSchema,
) : SkribePotentiallyScalarSchema {

  override fun isScalar(): Boolean {
    if (items is SkribeScalarSchema) return true
    if (items is SkribePotentiallyScalarSchema) return items.isScalar()
    return false
  }

  context(SkribeSpec)
  override fun toKotlinTypeName(): TypeName = List::class.asClassName().parameterizedBy(
    when (items.requiresSerialization) {
      true -> items.toKotlinTypeName().copy(
        annotations = listOf(
          AnnotationSpec.builder(Serializable::class).apply {
            addMember("with = %T::class", (items as SerializableSchema).serializerTypeName)
          }.build()
        )
      )

      false -> items.toKotlinTypeName()
    }
  )
}
