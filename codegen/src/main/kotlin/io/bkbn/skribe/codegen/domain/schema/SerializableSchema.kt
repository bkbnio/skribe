package io.bkbn.skribe.codegen.domain.schema

import com.squareup.kotlinpoet.TypeName

sealed interface SerializableSchema {
  val utilPackage: String
  val serializerTypeName: TypeName
}
