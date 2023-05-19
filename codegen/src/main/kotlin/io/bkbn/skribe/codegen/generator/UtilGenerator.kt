package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.swagger.v3.oas.models.OpenAPI
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class UtilGenerator(override val basePackage: String, override val openApi: OpenAPI) : Generator {

  private companion object {
    val uuidType = ClassName("com.benasher44.uuid", "Uuid")
  }

  fun generate(): Map<String, FileSpec> {
    return mapOf(
      "Serializers" to generateSerializers()
    )
  }

  private fun generateSerializers(): FileSpec = FileSpec.builder(utilPackage, "Serializers").apply {
    addUuidSerializer()
    addNumberSerializer()
  }.build()

  private fun FileSpec.Builder.addUuidSerializer() {
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
            returns(uuidType)
            val uf = MemberName("com.benasher44.uuid", "uuidFrom")
            addStatement("return %M(decoder.decodeString())", uf)
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

  private fun FileSpec.Builder.addNumberSerializer() {
    addType(
      TypeSpec.objectBuilder("NumberSerializer").apply {
        addSuperinterface(KSerializer::class.parameterizedBy(Number::class))
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
            returns(Number::class)
            addCode(
              CodeBlock.of(
                "return try {\n  decoder.decodeDouble()\n} catch (e: %T) {\n  decoder.decodeInt()\n}\n",
                NumberFormatException::class
              )
            )
          }.build()
        )
        addFunction(
          FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("encoder", Encoder::class)
            addParameter("value", Number::class)
            addStatement("encoder.encodeString(value.toString())")
          }.build()
        )
      }.build()
    )
  }
}
