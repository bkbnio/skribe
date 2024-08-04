package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.bkbn.skribe.codegen.converter.ConverterMetadata
import io.bkbn.skribe.codegen.converter.SpecConverter
import io.bkbn.skribe.codegen.generator.EnumGenerator
import io.bkbn.skribe.codegen.generator.ModelGenerator
import io.bkbn.skribe.codegen.generator.RequestGenerator
import io.bkbn.skribe.codegen.generator.SerializerGenerator
import io.bkbn.skribe.codegen.generator.TypeAliasGenerator
import io.swagger.parser.OpenAPIParser

object Skribe {
  fun generate(specUrl: String, rootPackage: String): List<FileSpec> {
    val spec = OpenAPIParser().readLocation(specUrl, null, null)
    val metadata = ConverterMetadata(rootPackage)

    val skribeSpec = with(metadata) { SpecConverter.convert(spec.openAPI) }

    return with(skribeSpec) {
      val enums = EnumGenerator.generate()
      val models = ModelGenerator.generate()
      val serializers = SerializerGenerator.generate()
      val requests = RequestGenerator.generate()
      val typeAliases = TypeAliasGenerator.generate()

      enums + models + serializers + requests + typeAliases
    }
  }
}
