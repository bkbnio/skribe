package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

data object SerializerGenerator : Generator {
  context(SkribeSpec) override fun generate(): List<FileSpec> {
    return listOf(
      FileSpec.builder("$rootPackage.util", "Serializers").apply {
        generateUuidSerializer()
        generateNumberSerializer()
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

        addFunction(
          FunSpec.builder("deserialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("decoder", Decoder::class)
            addStatement("return %T.fromString(decoder.decodeString())", uuidType)
          }.build()
        )

        addFunction(
          FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("encoder", Encoder::class)
            addParameter("value", uuidType)
            addStatement("encoder.encodeString(value.toString())")
          }.build()
        )
      }.build()
    )
  }

  private fun FileSpec.Builder.generateNumberSerializer() {
    val numberType = ClassName("kotlin", "Number")
    addType(
      TypeSpec.objectBuilder("NumberSerializer").apply {
        addSuperinterface(KSerializer::class.asClassName().parameterizedBy(numberType))
        addProperty(
          PropertySpec.builder("descriptor", SerialDescriptor::class).apply {
            addModifiers(KModifier.OVERRIDE)
            val psd = MemberName("kotlinx.serialization.descriptors", "PrimitiveSerialDescriptor")
            initializer("%M(%S, %T.DOUBLE)", psd, "Number", PrimitiveKind::class)
          }.build()
        )

        addFunction(
          FunSpec.builder("deserialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("decoder", Decoder::class)
            addStatement("return decoder.decodeDouble()")
          }.build()
        )

        addFunction(
          FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("encoder", Encoder::class)
            addParameter("value", numberType)
            addStatement("encoder.encodeString(value.toString())")
          }.build()
        )
      }.build()
    )
  }
}
