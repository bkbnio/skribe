package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.bkbn.skribe.codegen.domain.SkribeSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor

data object SerializerGenerator : Generator {
  context(SkribeSpec) override fun generate(): List<FileSpec> {
    return listOf(
      FileSpec.builder("$rootPackage.util", "Serializers").apply {
        generateUuidSerializer()
      }.build()
    )
  }

  private fun FileSpec.Builder.generateUuidSerializer() {
    val uuidType = ClassName("com.benasher44.uuid", "Uuid")
    addType(
      TypeSpec.objectBuilder("UuidSerializer").apply {
        addSuperinterface(KSerializer::class.asClassName().parameterizedBy(uuidType))
        addProperty(
          PropertySpec.builder("descriptor", SerialDescriptor::class).apply {
            addModifiers(KModifier.OVERRIDE)
            val psd = MemberName("kotlinx.serialization.descriptors", "PrimitiveSerialDescriptor")
            initializer("%M(%S, %T.STRING)", psd, "UUID", PrimitiveKind::class)
          }.build()
        )
      }.build()
    )
  }
}
