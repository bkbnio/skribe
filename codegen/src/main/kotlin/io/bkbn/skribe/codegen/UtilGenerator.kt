package io.bkbn.skribe.codegen

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.swagger.v3.oas.models.OpenAPI
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class UtilGenerator(override val basePackage: String, override val openApi: OpenAPI) : Generator {

  fun generate(): Map<String, FileSpec> {
    return mapOf(
      "Serializers" to generateSerializers()
    )
  }

  private fun generateSerializers(): FileSpec = FileSpec.builder(utilPackage, "Serializers").apply {
    addType(
      TypeSpec.objectBuilder("UuidSerializer").apply {
        addSuperinterface(KSerializer::class.parameterizedBy(Uuid::class))
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
            returns(Uuid::class)
            val uf = MemberName("com.benasher44.uuid", "uuidFrom")
            addStatement("return %M(decoder.decodeString())", uf)
          }.build()
        )
        addFunction(
          FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("encoder", Encoder::class)
            addParameter("value", Uuid::class)
            addStatement("encoder.encodeString(value.toString())")
          }.build()
        )
      }.build()
    )
  }.build()
}
