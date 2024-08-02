package io.bkbn.skribe.codegen.converter

import io.bkbn.skribe.codegen.domain.SkribePath
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem

data object PathConverter : Converter<Map<String, PathItem>, List<SkribePath>> {

  context(ConverterMetadata)
  override fun convert(input: Map<String, PathItem>): List<SkribePath> = input.map { (path, item) ->
    val paths = mutableListOf<SkribePath>()
    item.get?.let { paths.add(createOperation(SkribePath.Operation.GET, path, it)) }
    item.put?.let { paths.add(createOperation(SkribePath.Operation.PUT, path, it)) }
    item.post?.let { paths.add(createOperation(SkribePath.Operation.POST, path, it)) }
    item.delete?.let { paths.add(createOperation(SkribePath.Operation.DELETE, path, it)) }
    item.patch?.let { paths.add(createOperation(SkribePath.Operation.PATCH, path, it)) }
    item.head?.let { paths.add(createOperation(SkribePath.Operation.HEAD, path, it)) }
    item.options?.let { paths.add(createOperation(SkribePath.Operation.OPTIONS, path, it)) }
    paths
  }.flatten()

  private fun createOperation(operationType: SkribePath.Operation, path: String, operation: Operation): SkribePath {
    return SkribePath(
      path = path,
      operation = operationType,
    )
  }
}
