package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.FileSpec
import io.swagger.parser.OpenAPIParser

object ApiClientGenerator {
  fun generate(specUrl: String, basePackage: String): List<FileSpec> {
    val spec = OpenAPIParser().readLocation(specUrl, null, null)
    val models = ModelGenerator(basePackage, spec.openAPI).generate()
    val requests = RequestGenerator(basePackage, spec.openAPI).generate()
    val util = UtilGenerator(basePackage, spec.openAPI).generate()
    return models.values + requests.values + util.values
  }
}
