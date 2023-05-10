package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.swagger.parser.OpenAPIParser

object ApiClientGenerator {
  fun generate(specUrl: String, basePackage: String): List<FileSpec> {
    val spec = OpenAPIParser().readLocation(specUrl, null, null)
    val models = ModelGenerator(spec.openAPI, basePackage).generate()
    val requests = RequestGenerator(spec.openAPI, basePackage).generate()
    val util = UtilGenerator(basePackage).generate()
    return models.values + requests.values + util.values
  }
}
