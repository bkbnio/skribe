package io.bkbn.skribe.codegen

import io.swagger.v3.oas.models.OpenAPI

internal sealed interface Generator {
  val basePackage: String
  val openApi: OpenAPI
  val requestPackage
    get() = "$basePackage.requests"
  val modelPackage
    get() = "$basePackage.models"
  val utilPackage
    get() = "$basePackage.util"
}
