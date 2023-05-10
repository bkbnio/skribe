package io.bkbn.skribe.codegen

//import com.squareup.kotlinpoet.AnnotationSpec
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.FileSpec
//import com.squareup.kotlinpoet.FunSpec
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.MemberName
//import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.TypeAliasSpec
//import com.squareup.kotlinpoet.TypeName
//import com.squareup.kotlinpoet.TypeSpec
//import com.squareup.kotlinpoet.asClassName
//import io.ktor.client.HttpClient
//import io.ktor.client.statement.HttpResponse
//import io.ktor.http.HttpMethod
//import java.util.Locale
//
//internal class OpenApiClientGenerator(private val basePackage: String) {
//  fun generate(spek: OpenApi): List<FileSpec> {
//    val models = generateModels(spek)
//    val requests = generateRequests(spek)
//    return models + requests
//  }
//
//  private fun generateModels(spek: OpenApi): List<FileSpec> = spek.components.schemas.map { (name, schema) ->
//    FileSpec.builder("$basePackage.models", name).apply {
//      generateModelFromSchema(spek, name, schema)
//    }.build()
//  }
//
//  // TODO Clean this up
//  @Suppress("CyclomaticComplexMethod")
//  private fun FileSpec.Builder.generateModelFromSchema(parentSpec: OpenApi, name: String, schema: Schema) {
//    when (schema) {
//      is AllOfSchema -> generateAllOfSchema(parentSpec, schema, name)
//      is AnyOfSchema -> {}
//      is ArraySchema -> {}
//      is BooleanSchema -> {}
//      FreeFormSchema -> error("FreeFormSchema is not currently supported")
//      is IntegerSchema -> {
//        addTypeAlias(TypeAliasSpec.builder(name, Int::class.asClassName()).build())
//      }
//
//      is ObjectSchema -> generateObjectSchema(name, schema)
//      is OneOfSchema -> {}
//      is ReferenceSchema -> {}
//      is StringSchema -> {
//        // TODO Date and DateTime and stuff?
//        if (schema.enum?.isNotEmpty() == true) {
//          addType(
//            TypeSpec.enumBuilder(name).apply {
//              schema.enum?.filterNotNull()?.forEach { enumValue ->
//                if (enumValue.isSnake() || enumValue.isCamel()) {
//                  val formattedValue = enumValue.toAngrySnake()
//                  addEnumConstant(
//                    formattedValue,
//                    TypeSpec.anonymousClassBuilder().apply {
//                      addAnnotation(
//                        AnnotationSpec.builder(ClassName("kotlinx.serialization", "SerialName"))
//                          .addMember("%S", enumValue).build()
//                      )
//                    }.build()
//                  )
//                } else {
//                  addEnumConstant(enumValue)
//                }
//              }
//            }.build()
//          )
//        } else {
//          addTypeAlias(TypeAliasSpec.builder(name, String::class.asClassName()).build())
//        }
//      }
//    }
//  }
//
//  private fun FileSpec.Builder.generateAllOfSchema(parentSpec: OpenApi, schema: AllOfSchema, name: String) {
//    val allOf = schema.allOf
//    require(allOf.all { it is ReferenceSchema }) {
//      "Currently, all members of an allOf schema must be ReferenceSchemas"
//    }
//    val references = allOf.map { it as ReferenceSchema }.map { it.`$ref` }.map { it.substringAfterLast("/") }
//    val schemas = references.map { parentSpec.components.schemas[it]!! }
//    require(schemas.all { it is ObjectSchema }) {
//      "Currently, references within an allOf schema must point to ObjectSchemas"
//    }
//    val objectSchemas = schemas.map { it as ObjectSchema }
//    val gigaSchema = ObjectSchema(
//      properties = objectSchemas.map { it.properties }.flatMap { it.entries }.associate { it.key to it.value },
//      required = objectSchemas.flatMap { it.required }.toList()
//    )
//    generateObjectSchema(name, gigaSchema)
//  }
//
//  private fun FileSpec.Builder.generateObjectSchema(name: String, schema: ObjectSchema) {
//    if (schema.properties.isEmpty()) {
//      addType(
//        TypeSpec.objectBuilder(name).apply {
//          addAnnotation(AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build())
//        }.build()
//      )
//      return
//    }
//    addType(
//      TypeSpec.classBuilder(name).apply {
//        addModifiers(KModifier.DATA)
//        addAnnotation(
//          AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build()
//        )
//        schema.properties.filter { it.value is ObjectSchema }
//          .mapValues { it.value as ObjectSchema }
//          .forEach { (k, v) -> generateObjectSchema("${name}${k.capitalized()}", v) }
//        primaryConstructor(
//          FunSpec.constructorBuilder().apply {
//            schema.properties.forEach { (propName, propSchema) ->
//              val sanitizedName = propName.sanitizePropertyName()
//              val valName = if (sanitizedName.isSnake()) sanitizedName.snakeToCamel() else sanitizedName
//              addParameter(valName, propSchema.toTypeName(name, valName))
//            }
//          }.build()
//        )
//        schema.properties.forEach { (propName, propSchema) ->
//          val sanitizedName = propName.sanitizePropertyName()
//          val valName = if (sanitizedName.isSnake()) sanitizedName.snakeToCamel() else sanitizedName
//          addProperty(
//            PropertySpec.builder(valName, propSchema.toTypeName(name, valName)).apply {
//              initializer(valName)
//              if (valName != propName) {
//                addAnnotation(
//                  AnnotationSpec.builder(ClassName("kotlinx.serialization", "SerialName")).apply {
//                    addMember("\"$propName\"")
//                  }.build()
//                )
//              }
//            }.build()
//          )
//        }
//      }.build()
//    )
//  }
//
//  private fun Schema.toTypeName(parentName: String, propName: String): TypeName = when (this) {
//    is AllOfSchema -> error("AllOfSchema is not currently supported")
//    is AnyOfSchema -> error("AnyOfSchema is not currently supported")
//    is ArraySchema -> List::class.asClassName().parameterizedBy(items.toTypeName("$parentName$propName", "Items"))
//    is BooleanSchema -> Boolean::class.asClassName()
//    FreeFormSchema -> error("FreeFormSchema is not currently supported")
//    is IntegerSchema -> Int::class.asClassName()
//    is ObjectSchema -> ClassName("$basePackage.models", "$parentName${propName.capitalized()}")
//    is OneOfSchema -> error("OneOfSchema is not currently supported")
//    is ReferenceSchema -> ClassName("$basePackage.models", `$ref`.substringAfterLast("/"))
//    is StringSchema -> String::class.asClassName()
//  }
//
//  private fun generateRequests(spek: OpenApi): List<FileSpec> = spek.paths.map { (slug, path) ->
//    listOfNotNull(
//      path.get?.let { path.toRequest(it, HttpMethod.Get, slug) },
//      path.put?.let { path.toRequest(it, HttpMethod.Put, slug) },
//      path.post?.let { path.toRequest(it, HttpMethod.Post, slug) },
//      path.delete?.let { path.toRequest(it, HttpMethod.Delete, slug) },
//      path.options?.let { path.toRequest(it, HttpMethod.Options, slug) },
//      path.head?.let { path.toRequest(it, HttpMethod.Head, slug) },
//      path.patch?.let { path.toRequest(it, HttpMethod.Patch, slug) },
//      path.trace?.let { path.toRequest(it, HttpMethod("Trace"), slug) }
//    )
//  }.flatten()
//
//  @Suppress("LongMethod", "CyclomaticComplexMethod")
//  private fun Path.toRequest(operation: PathOperation, httpMethod: HttpMethod, slug: String): FileSpec {
//    var mutableSlug = slug
//    val name = operation.operationId?.capitalized() ?: error("Currently an operation id is required")
//    val responseClassFQNs =
//      operation.responses.toResponseFQDNs().joinToString(separator = "\n") { "\t- $it" }
//    return FileSpec.builder("$basePackage.requests", name).apply {
//      addFunction(
//        FunSpec.builder(operation.operationId!!).apply {
//          if (operation.description != null) {
//            addKdoc(operation.description!!)
//            addKdoc(
//              """
//              Body will be one of the following:
//              $responseClassFQNs
//            """.trimIndent()
//            )
//          }
//          addModifiers(KModifier.SUSPEND)
//          receiver(HttpClient::class)
//          returns(HttpResponse::class.asClassName())
//
//          if (operation.requestBody?.required == true) {
//            val requestBody = operation.requestBody!!
//            require(requestBody.content["application/json"] != null) {
//              "Currently, only json request bodies are supported"
//            }
//            require(requestBody.content["application/json"]!!.schema is ReferenceSchema) {
//              "Currently, only references are supported"
//            }
//            val reference = requestBody.content["application/json"]!!.schema as ReferenceSchema
//            val schema = reference.`$ref`.substringAfterLast("/")
//            addParameter("requestBody", ClassName("$basePackage.models", schema))
//          }
//
//          val parameters = this@toRequest.parameters.plus(operation.parameters).toSet()
//
//          parameters.forEach { parameter ->
//            when (parameter) {
//              is ReferenceParameter -> error("References are not currently supported")
//              is LiteralParameter -> {
//                var paramName = parameter.name.sanitizePropertyName()
//                paramName = if (paramName.isSnake()) paramName.snakeToCamel() else paramName
//                addParameter(paramName, String::class)
//
//                if (parameter.`in`.lowercase() == "path") {
//                  mutableSlug = replacePathParameter(mutableSlug, parameter.name, paramName)
//                }
//              }
//            }
//          }
//
//          addCode(
//            CodeBlock.builder().apply {
//              beginControlFlow("return %M(%P)", httpMethod.toMemberName(), mutableSlug)
//              beginControlFlow("url")
//              parameters.forEach { parameter ->
//                when (parameter) {
//                  is ReferenceParameter -> error("References are not currently supported")
//                  is LiteralParameter -> {
//                    var paramName = parameter.name.sanitizePropertyName()
//                    paramName = if (paramName.isSnake()) paramName.snakeToCamel() else paramName
//                    if (parameter.`in`.lowercase() == "query") {
//                      addStatement("parameters.append(%S, %L)", paramName, paramName)
//                    }
//                  }
//                }
//              }
//              endControlFlow()
//              endControlFlow()
//            }.build()
//          )
//        }.build()
//      )
//    }.build()
//  }
//
//  private fun replacePathParameter(path: String, key: String, replacement: String): String {
//    val pattern = "\\{$key}".toRegex()
//    return pattern.replace(path) {
//      "$$replacement"
//    }
//  }
//
//  private fun String.capitalized() =
//    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
//
//  private fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z]+)+$"))
//
//  private fun String.isCamel() = matches(Regex("^[a-z]+(?:[A-Z][a-z]*)*$"))
//
//  private fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
//    if (index == 0) word else word.capitalized()
//  }.joinToString("")
//
//  private fun String.toAngrySnake(): String {
//    require(isSnake() || isCamel()) { "The provided string is neither in snake_case nor camelCase." }
//    val snakeCaseValue = if (isCamel()) {
//      replace(Regex("([A-Z])"), "_$1").lowercase(Locale.getDefault())
//    } else {
//      this
//    }
//
//    return snakeCaseValue.uppercase(Locale.getDefault())
//  }
//
//  private fun String.sanitizePropertyName(): String = trim().replace(Regex("\\s+"), "_")
//
//  private fun HttpMethod.toMemberName() = when (this) {
//    HttpMethod.Get -> MemberName("io.ktor.client.request", "get")
//    HttpMethod.Put -> MemberName("io.ktor.client.request", "put")
//    HttpMethod.Post -> MemberName("io.ktor.client.request", "post")
//    HttpMethod.Delete -> MemberName("io.ktor.client.request", "delete")
//    HttpMethod.Options -> MemberName("io.ktor.client.request", "options")
//    HttpMethod.Head -> MemberName("io.ktor.client.request", "head")
//    HttpMethod.Patch -> MemberName("io.ktor.client.request", "patch")
//    HttpMethod("Trace") -> MemberName("io.ktor.client.request", "trace")
//    else -> error("Unsupported http method: $this")
//  }
//
//  private fun Map<String, Response>.toResponseFQDNs() =
//    values.map { response -> response.content["application/json"]?.schema.toFQDN() }
//
//  private fun Schema?.toFQDN(): TypeName = when (this) {
//    is AllOfSchema -> TODO()
//    is AnyOfSchema -> TODO()
//    is ArraySchema -> List::class.asClassName().parameterizedBy(items.toFQDN())
//    is BooleanSchema -> TODO()
//    FreeFormSchema -> TODO()
//    is IntegerSchema -> TODO()
//    is ObjectSchema -> TODO()
//    is OneOfSchema -> TODO()
//    is ReferenceSchema -> ClassName("$basePackage.models", `$ref`.substringAfterLast("/"))
//    is StringSchema -> TODO()
//    null -> Unit::class.asClassName()
//  }
//}
