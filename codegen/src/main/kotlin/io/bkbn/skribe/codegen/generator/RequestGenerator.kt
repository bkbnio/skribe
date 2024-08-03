package io.bkbn.skribe.codegen.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asTypeName
import io.bkbn.skribe.codegen.domain.SkribePath
import io.bkbn.skribe.codegen.domain.SkribeSpec
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse

data object RequestGenerator : Generator {

  private const val KTOR_REQUEST_PACKAGE = "io.ktor.client.request"

  context(SkribeSpec)
  override fun generate(): List<FileSpec> = paths.map { path ->
    val requestPackage = "$rootPackage.request"
    FileSpec.builder(requestPackage, path.name.fileName()).apply {
      with(path) { addFunction(toRequest()) }
    }.build()
  }

  context(SkribePath, SkribeSpec)
  private fun toRequest(): FunSpec {
    return FunSpec.builder(name.addressableName()).apply {
      receiver(HttpClient::class.asTypeName())
      returns(HttpResponse::class)
      addModifiers(KModifier.SUSPEND)
      addRequestBodyIfPresent()
      addCode(CodeBlock.builder().apply {
        beginControlFlow("return %M(%P)", operation.toRequestMethod(), path)
        setBodyInRequestIfPresent()
        endControlFlow()
      }.build())
      description?.let { addKdoc(it) }
    }.build()
  }

  private fun SkribePath.Operation.toRequestMethod(): MemberName =
    MemberName(KTOR_REQUEST_PACKAGE, this.name.lowercase())

  context(SkribePath, SkribeSpec)
  private fun FunSpec.Builder.addRequestBodyIfPresent() {
    val requestBody = requestBody ?: return
    addParameter("body", requestBody.schema.toKotlinTypeName())
  }

  context(SkribePath)
  private fun CodeBlock.Builder.setBodyInRequestIfPresent() {
    requestBody ?: return
    addStatement("%M(body)", MemberName(KTOR_REQUEST_PACKAGE, "setBody"))
  }
}

/*
/**
 * Return the response data for the underlying batch request that is specified by the id. Body can
 * be one of the following types:
 * 	- [io.bkbn.sourdough.factset.prices.models.BatchDataResponse]
 * 	- [io.bkbn.sourdough.factset.prices.models.BatchStatusResponse]
 * 	- [io.bkbn.sourdough.factset.prices.models.`404`]
 */
public suspend fun HttpClient.getBatchDataWithPost(body: BatchDataRequest) =
    post("""/batch/v1/result""") {
  setBody(body)
}

 */
