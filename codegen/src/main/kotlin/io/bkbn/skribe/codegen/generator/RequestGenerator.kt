package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.bkbn.skribe.codegen.utils.SchemaUtils.enumConstants
import io.bkbn.skribe.codegen.utils.SchemaUtils.safeRequired
import io.bkbn.skribe.codegen.utils.StringUtils.capitalized
import io.bkbn.skribe.codegen.utils.StringUtils.convertToCamelCase
import io.bkbn.skribe.codegen.utils.StringUtils.getRefKey
import io.ktor.client.HttpClient
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import java.lang.StringBuilder

class RequestGenerator(
  override val basePackage: String,
  override val openApi: OpenAPI
) : Generator {

  fun generate(): Map<String, FileSpec> =
    openApi.paths.mapValues { (path, pathItem) ->
      pathItem.createRequestMethodFiles(path) +
        pathItem.createResponseTypeFiles() +
        pathItem.createRequestTypeFiles() +
        pathItem.createParameterTypeFiles()
    }
      .values
      .flatMap { it.entries }
      .associate { it.key to it.value }

  private fun PathItem.createRequestMethodFiles(path: String): Map<String, FileSpec> =
    readOperationsMap().entries.associate { (method, operation) ->
      operation.operationId to operation.createRequestFile(path, method, this)
    }

  private fun PathItem.createResponseTypeFiles(): Map<String, FileSpec> =
    readOperationsMap().entries.associate { (_, operation) ->
      "${operation.operationId}Response" to operation.createResponseFile()
    }.filterValues { it != null }.mapValues { it.value!! }

  private fun PathItem.createRequestTypeFiles(): Map<String, FileSpec> =
    readOperationsMap().entries.associate { (_, operation) ->
      "${operation.operationId}Request" to operation.createRequestBodyFile()
    }.filterValues { it != null }.mapValues { it.value!! }

  private fun PathItem.createParameterTypeFiles(): Map<String, FileSpec> =
    readOperationsMap().entries.associate { (_, operation) ->
      "${operation.operationId}Parameter" to operation.createParameterFile()
    }.filterValues { it != null }.mapValues { it.value!! }

  private fun Operation.createRequestFile(path: String, method: HttpMethod, pathItem: PathItem): FileSpec =
    FileSpec.builder(requestPackage, operationId.capitalized()).apply {
      addFunction(createRequestFunction(path, method, pathItem))
    }.build()

  private fun Operation.createResponseFile(): FileSpec? {
    val inlineResponseTypes = collectInlineResponseTypes()
    if (inlineResponseTypes.isEmpty()) return null
    return FileSpec.builder(modelPackage, operationId.capitalized().plus("Response")).apply {
      // TODO: Fix
      addSchemaType(operationId.capitalized(), inlineResponseTypes.first())
//      inlineResponseTypes.forEach { addSchemaType(operationId.capitalized(), it) }
    }.build()
  }

  private fun Operation.createRequestBodyFile(): FileSpec? {
    val inlineRequestType = collectInlineRequestTypes() ?: return null
    return FileSpec.builder(modelPackage, operationId.capitalized().plus("Request")).apply {
      addSchemaType(operationId.capitalized().plus("Request"), inlineRequestType)
    }.build()
  }

  private fun Operation.createParameterFile(): FileSpec? {
    val inlineParameterTypes = collectInlineParameterTypes()
    if (inlineParameterTypes.isEmpty()) return null
    return FileSpec.builder(modelPackage, operationId.capitalized().plus("Param")).apply {
      inlineParameterTypes.forEach { (name, schema) ->
        addSchemaType(
          name,
          schema
        )
      }
    }.build()
  }

  private fun Operation.collectInlineResponseTypes(): List<Schema<*>> = responses
    .filterValues { it.content != null }
    .values
    .mapNotNull { response ->
      val content = response.content

      if (response.content.isEmpty()) return@mapNotNull null

      val contentType = content.keys.first()
      content[contentType]?.schema
    }
    .filter { it.`$ref` == null || (it is ArraySchema && it.items.`$ref` == null) }

  private fun Operation.collectInlineRequestTypes(): Schema<*>? =
    requestBody?.content?.values?.mapNotNull { content ->
      content.schema
    }?.firstOrNull {
      it.`$ref` == null || (it is ArraySchema && it.items.`$ref` == null)
    }

  private fun Operation.collectInlineParameterTypes(): Map<String, Schema<*>> =
    (parameters ?: emptyList())
      .filter { it.`$ref` == null }.associate { parameter ->
        operationId.capitalized().plus(parameter.name.convertToCamelCase().capitalized()) to parameter.schema
      }.filterValues { it is StringSchema && it.enumConstants.isNotEmpty() }

  private fun Operation.createRequestFunction(path: String, method: HttpMethod, pathItem: PathItem): FunSpec =
    FunSpec.builder(operationId).apply {
      var mutablePath = path
      val allParams = (this@createRequestFunction.parameters ?: emptyList()) + (pathItem.parameters ?: emptyList())
      val queryParams = allParams.filter { it.`in` == "query" }
      val pathParams = allParams.filter { it.`in` == "path" }

      pathParams.forEach { param ->
        mutablePath =
          replacePathParameter(mutablePath, param.name, param.name.convertToCamelCase())
      }

      val bodyType = requestBody?.content?.values?.first()?.schema?.toKotlinTypeName(operationId.plus("Request"))

      if (bodyType != null) {
        addParameter(
          ParameterSpec.builder("body", bodyType.copy(nullable = requestBody.safeRequired.not())).build()
        )
      }

      receiver(HttpClient::class)
      addModifiers(KModifier.SUSPEND)
      description?.let { addKdoc("%L", it.replace("/", "")) }
      addTypeHints(this@createRequestFunction)
      attachParameters(this@createRequestFunction, pathItem.parameters?.toList() ?: emptyList())
      val ktorMember = method.toKtorMemberName()
      beginControlFlow("return %M(%P)", ktorMember, mutablePath)
      if (bodyType != null) attachRequestBody(requestBody.safeRequired.not())
      if (queryParams.isNotEmpty()) attachQueryParameters(queryParams)
      endControlFlow()
    }.build()

  private fun HttpMethod.toKtorMemberName() = when (this) {
    HttpMethod.POST -> MemberName("io.ktor.client.request", "post")
    HttpMethod.GET -> MemberName("io.ktor.client.request", "get")
    HttpMethod.PUT -> MemberName("io.ktor.client.request", "put")
    HttpMethod.PATCH -> MemberName("io.ktor.client.request", "patch")
    HttpMethod.DELETE -> MemberName("io.ktor.client.request", "delete")
    HttpMethod.HEAD -> MemberName("io.ktor.client.request", "head")
    HttpMethod.OPTIONS -> MemberName("io.ktor.client.request", "options")
    HttpMethod.TRACE -> MemberName("io.ktor.client.request", "trace")
  }

  private fun FunSpec.Builder.attachParameters(operation: Operation, pathParameters: List<Parameter>) {
    val allParams = ((operation.parameters ?: emptyList()) + pathParameters).filter { it.`$ref` == null }
    allParams.forEach { parameter ->
      addParameter(
        if (parameter.schema is StringSchema && parameter.schema.enumConstants.isNotEmpty()) {
          ParameterSpec.builder(
            parameter.name.convertToCamelCase(),
            ClassName(
              modelPackage,
              operation.operationId.capitalized().plus(parameter.name.convertToCamelCase().capitalized())
            )
          ).build()
        } else {
          ParameterSpec.builder(
            parameter.name.convertToCamelCase(),
            // TODO: Fix hack -> parameter.content.values.first().schema
            (parameter.schema ?: parameter.content.values.first().schema).toKotlinTypeName(operation.operationId)
              .copy(nullable = parameter.safeRequired.not())
          ).build()
        }
      )
    }
  }

  private fun FunSpec.Builder.attachQueryParameters(parameters: List<Parameter>) {
    addCode(
      CodeBlock.builder().apply {
        beginControlFlow("url")
        parameters.forEach { param ->
          if (param.safeRequired) {
            addStatement("parameters.append(%S, %L.toString())", param.name, param.name.convertToCamelCase())
          } else {
            addStatement(
              "%L?.let { parameters.append(%S, it.toString()) }",
              param.name.convertToCamelCase(),
              param.name
            )
          }
        }
        endControlFlow()
      }.build()
    )
  }

  private fun FunSpec.Builder.attachRequestBody(nullable: Boolean) {
    val bodyMn = MemberName("io.ktor.client.request", "setBody")
    addCode(
      CodeBlock.builder().apply {
        if (nullable) {
          addStatement("body?.let { %M(it) }", bodyMn)
        } else {
          addStatement("%M(body)", bodyMn)
        }
      }.build()
    )
  }

  private fun replacePathParameter(path: String, key: String, replacement: String): String {
    val pattern = "\\{$key}".toRegex()
    return pattern.replace(path) {
      "$$replacement"
    }
  }

  private fun Operation.collectPossibleResponseTypes(): List<TypeName?> = responses.values.map { response ->
    when {
      response.`$ref` != null -> ClassName(modelPackage, response.`$ref`.getRefKey())
      response.content != null -> {
        val content = response.content

        if (response.content.isEmpty()) return@map Unit::class.asTypeName()

        val contentType = content.keys.first()
        val schema = content[contentType]?.schema
        when {
          schema?.`$ref` != null -> ClassName(modelPackage, schema.`$ref`.getRefKey())
          schema != null -> schema.toKotlinTypeName(operationId)
          else -> error("Unknown response type: $response")
        }
      }

      else -> null
    }
  }

  private fun FunSpec.Builder.addTypeHints(operation: Operation) {
    val responseTypes = operation.collectPossibleResponseTypes()
    val responseBuilder = StringBuilder()
    responseBuilder.append("Body can be one of the following types:\n")
    responseTypes.toSet().forEach { responseBuilder.append("\t- [$it]\n") }
    addKdoc(responseBuilder.toString())
  }
}
