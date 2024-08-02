package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.bkbn.skribe.codegen.domain.schema.SkribeEnumSchema
import io.bkbn.skribe.codegen.domain.SkribeSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data object EnumGenerator : Generator {

  context(SkribeSpec)
  override fun generate(packageName: String): List<FileSpec> =
    schemas.filterIsInstance<SkribeEnumSchema>().map { schema ->
      FileSpec.builder(packageName, schema.addressableName()).apply {
        with(schema) {
          addType(toEnumType())
        }
      }.build()
    }

  context(SkribeEnumSchema)
  fun toEnumType(): TypeSpec {
    return TypeSpec.enumBuilder(addressableName()).apply {
      addAnnotation(AnnotationSpec.builder(Serializable::class).build())
      values.forEach { value ->
        addEnumConstant(value.addressableName(), TypeSpec.anonymousClassBuilder().apply {
          if (value.requiresSerialization()) {
            addAnnotation(AnnotationSpec.builder(SerialName::class).apply {
              addMember("%S", value.value)
            }.build())
          }
        }.build())
      }
    }.build()
  }
}
