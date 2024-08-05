package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribePath
import io.bkbn.skribe.codegen.utils.StringUtils.capitalized
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter

data object PathConverter : Converter<Map<String, PathItem>, List<SkribePath>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, PathItem>): List<SkribePath> = input.map { (path, item) ->
    val paths = mutableListOf<SkribePath>()
    val pathParams = item.parameters ?: emptyList()
    item.get?.let { paths.add(createOperation(SkribePath.Operation.GET, path, it, pathParams)) }
    item.put?.let { paths.add(createOperation(SkribePath.Operation.PUT, path, it, pathParams)) }
    item.post?.let { paths.add(createOperation(SkribePath.Operation.POST, path, it, pathParams)) }
    item.delete?.let { paths.add(createOperation(SkribePath.Operation.DELETE, path, it, pathParams)) }
    item.patch?.let { paths.add(createOperation(SkribePath.Operation.PATCH, path, it, pathParams)) }
    item.head?.let { paths.add(createOperation(SkribePath.Operation.HEAD, path, it, pathParams)) }
    item.options?.let { paths.add(createOperation(SkribePath.Operation.OPTIONS, path, it, pathParams)) }
    item.trace?.let { paths.add(createOperation(SkribePath.Operation.TRACE, path, it, pathParams)) }
    paths
  }.flatten()

  context(ConverterMetadata)
  private fun createOperation(
    operationType: SkribePath.Operation,
    path: String,
    operation: Operation,
    pathParameters: List<Parameter>,
  ): SkribePath {
    return SkribePath(
      path = path,
      operation = operationType,
      name = SkribePath.PathName(
        operation.operationId
          ?: operation.summary.split(" ")
            .filterNot { it.isEmpty() }
            .joinToString("") {
              it.capitalized()
            }
      ),
      description = operation.description,
      requestBody = operation.requestBody?.let { RequestBodyConverter.convert(it) },
      responses = operation.responses?.let { ResponseConverter.convert(it) } ?: emptyList(),
      pathParameters = pathParameters.associateBy { it.name ?: it.`$ref` }.let { ParameterConverter.convert(it) },
      operationParameters = operation.parameters?.associateBy { it.name ?: it.`$ref` }?.let {
        ParameterConverter.convert(
          it
        )
      }
        ?: emptyList(),
    )
  }
}
