package io.bkbn.skribe.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import io.ktor.client.HttpClient
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import java.lang.StringBuilder

class RequestGenerator(
  override val basePackage: String,
  override val openApi: OpenAPI
) : Generator {

  fun generate(): Map<String, FileSpec> =
    openApi.paths.mapValues { (path, pathItem) ->
      pathItem.createRequestMethodFiles(path) + pathItem.createResponseTypeFiles()
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

  private fun Operation.createRequestFile(path: String, method: HttpMethod, pathItem: PathItem): FileSpec =
    FileSpec.builder(requestPackage, operationId.capitalized()).apply {
      addFunction(createRequestFunction(path, method, pathItem))
    }.build()

  private fun Operation.createResponseFile(): FileSpec? {
    val inlineResponseTypes = collectInlineResponseTypes()
    if (inlineResponseTypes.isEmpty()) return null
    return FileSpec.builder(modelPackage, operationId.capitalized()).apply {
      inlineResponseTypes.forEach { addSchemaType(operationId.capitalized(), it) }
    }.build()
  }

  private fun Operation.collectInlineResponseTypes(): List<Schema<*>> = responses
    .filterValues { it.content != null }
    .values
    .mapNotNull { response ->
      val content = response.content
      val contentType = content.keys.first()
      content[contentType]?.schema
    }
    .filter { it.`$ref` == null || (it is ArraySchema && it.items.`$ref` == null) }

  private fun Operation.createRequestFunction(path: String, method: HttpMethod, pathItem: PathItem): FunSpec =
    FunSpec.builder(operationId).apply {
      var mutablePath = path
      val allParams = (this@createRequestFunction.parameters ?: emptyList()) + (pathItem.parameters ?: emptyList())
      val queryParams = allParams.filter { it.`in` == "query" }
      val pathParams = allParams.filter { it.`in` == "path" }

      pathParams.forEach { param ->
        mutablePath =
          replacePathParameter(mutablePath, param.name, param.name.formattedParamName())
      }

      val bodyType = requestBody?.content?.values?.first()?.schema?.toKotlinTypeName(operationId)

      if (bodyType != null) {
        addParameter(
          ParameterSpec.builder("body", bodyType.copy(nullable = requestBody.required.not())).build()
        )
      }

      receiver(HttpClient::class)
      addModifiers(KModifier.SUSPEND)
      description?.let { addKdoc(it) }
      addTypeHints(this@createRequestFunction)
      attachParameters(this@createRequestFunction, pathItem.parameters?.toList() ?: emptyList())
      val ktorMember = method.toKtorMemberName()
      beginControlFlow("return %M(%P)", ktorMember, mutablePath)
      if (bodyType != null) attachRequestBody(requestBody.required.not())
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
    val allParams = (operation.parameters ?: emptyList()) + pathParameters
    allParams.forEach { parameter ->
      val parameterSchema = parameter.schema
      addParameter(
        ParameterSpec.builder(
          parameter.name.formattedParamName(),
          parameterSchema.toKotlinTypeName(operation.operationId).copy(nullable = parameter.required.not())
        ).build()
      )
    }
  }

  private fun FunSpec.Builder.attachQueryParameters(parameters: List<Parameter>) {
    addCode(
      CodeBlock.builder().apply {
        beginControlFlow("url")
        parameters.forEach { param ->
          if (param.required) {
            addStatement("parameters.append(%S, %L.toString())", param.name, param.name.formattedParamName())
          } else {
            addStatement(
              "%L?.let { parameters.append(%S, it.toString()) }",
              param.name.formattedParamName(),
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

  private fun String.formattedParamName(): String = this.sanitizePropertyName().snakeToCamel()

  private fun Operation.collectPossibleResponseTypes(): List<TypeName> = responses.values.map { response ->
    when {
      response.`$ref` != null -> ClassName(modelPackage, response.`$ref`.getRefKey())
      response.content != null -> {
        val content = response.content
        val contentType = content.keys.first()
        val schema = content[contentType]?.schema
        when {
          schema?.`$ref` != null -> ClassName(modelPackage, schema.`$ref`.getRefKey())
          schema != null -> schema.toKotlinTypeName(operationId)
          else -> error("Unknown response type: $response")
        }
      }

      else -> error("Unknown response type: $response")
    }
  }

  private fun FunSpec.Builder.addTypeHints(operation: Operation) {
    val responseTypes = operation.collectPossibleResponseTypes()
    val responseBuilder = StringBuilder()
    responseBuilder.append("Body can be one of the following types:\n")
    responseTypes.forEach { responseBuilder.append("\t- [$it]\n") }
    addKdoc(responseBuilder.toString())
  }
}
