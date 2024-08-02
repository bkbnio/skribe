package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.bkbn.skribe.codegen.converter.SpecConverter
import io.bkbn.skribe.codegen.generator.EnumGenerator
import io.bkbn.skribe.codegen.generator.ModelGenerator
import io.bkbn.skribe.codegen.generator.SerializerGenerator
import io.swagger.parser.OpenAPIParser

object Skribe {
  fun generate(specUrl: String, basePackage: String): List<FileSpec> {
    val spec = OpenAPIParser().readLocation(specUrl, null, null)
    val specConverter = SpecConverter(basePackage)
    val skribeSpec = specConverter.convert(spec.openAPI)

    val modelPackage = "$basePackage.models"
    val utilPackage = "$basePackage.util"

    return with(skribeSpec) {
      val enums = EnumGenerator.generate(modelPackage)
      val models = ModelGenerator.generate(modelPackage)
      val serializers = SerializerGenerator.generate(utilPackage)

      enums + models + serializers
    }
  }
}
