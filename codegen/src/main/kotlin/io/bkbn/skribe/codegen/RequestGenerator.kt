package io.bkbn.skribe.codegen

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.bkbn.skribe.codegen.Util.capitalized
import io.bkbn.skribe.codegen.Util.enumConstants
import io.bkbn.skribe.codegen.Util.getRefKey
import io.bkbn.skribe.codegen.Util.isReferenceSchema
import io.bkbn.skribe.codegen.Util.sanitizePropertyName
import io.ktor.client.HttpClient
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema

class RequestGenerator(private val spec: OpenAPI, basePackage: String) {

  private val requestPackage = "$basePackage.requests"
  private val modelPackage = "$basePackage.models"

  fun generate(): Map<String, FileSpec> = spec.paths.mapValues { (path, pathItem) -> pathItem.createRequestFiles(path) }
    .values.flatMap { it.entries }
    .associate { it.key to it.value }

  private fun PathItem.createRequestFiles(path: String): Map<String, FileSpec> =
    readOperationsMap().entries.associate { (method, operation) ->
      operation.operationId to operation.createRequestFile(path, method, this)
    }

  private fun Operation.createRequestFile(path: String, method: HttpMethod, pathItem: PathItem): FileSpec =
    FileSpec.builder(requestPackage, operationId.capitalized()).apply {
      addFunction(createRequestFunction(path, method, pathItem))
    }.build()

  private fun Operation.createRequestFunction(path: String, method: HttpMethod, pathItem: PathItem): FunSpec =
    FunSpec.builder(operationId).apply {
      receiver(HttpClient::class)
      addModifiers(KModifier.SUSPEND)
      attachParameters(this@createRequestFunction, pathItem.parameters?.toList() ?: emptyList())
      val ktorMember = when (method) {
        HttpMethod.POST -> MemberName("io.ktor.client.request", "post")
        HttpMethod.GET -> MemberName("io.ktor.client.request", "get")
        HttpMethod.PUT -> MemberName("io.ktor.client.request", "put")
        HttpMethod.PATCH -> MemberName("io.ktor.client.request", "patch")
        HttpMethod.DELETE -> MemberName("io.ktor.client.request", "delete")
        HttpMethod.HEAD -> MemberName("io.ktor.client.request", "head")
        HttpMethod.OPTIONS -> MemberName("io.ktor.client.request", "options")
        HttpMethod.TRACE -> MemberName("io.ktor.client.request", "trace")
      }
      beginControlFlow("return %M(%S)", ktorMember, path)
      addStatement("// TODO")
      endControlFlow()
    }.build()

  private fun Schema<*>.toKotlinTypeName(): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName())
    is UUIDSchema -> Uuid::class.asTypeName()
    is DateTimeSchema -> String::class.asTypeName() // todo switch to kotlinx datetime
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> Int::class.asTypeName()
    is StringSchema -> {
      when {
        enumConstants.isNotEmpty() -> TODO()
        else -> String::class.asTypeName()
      }
    }
    is BooleanSchema -> Boolean::class.asTypeName()
    else -> {
      when {
        isReferenceSchema() -> ClassName(modelPackage, `$ref`.getRefKey())
        else -> error("Unknown schema type: $this")
      }
    }
  }

  private fun FunSpec.Builder.attachParameters(operation: Operation, pathParameters: List<Parameter>) {
    val allParams = (operation.parameters ?: emptyList()) + pathParameters
    allParams.forEach { parameter ->
      val parameterSchema = parameter.schema
      addParameter(
        ParameterSpec.builder(parameter.name.sanitizePropertyName(), parameterSchema.toKotlinTypeName().copy(nullable = parameter.required.not())).build()
      )
    }
  }
}
