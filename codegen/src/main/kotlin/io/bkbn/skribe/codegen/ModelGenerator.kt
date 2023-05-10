package io.bkbn.skribe.codegen

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema
import kotlinx.serialization.Serializable
import java.util.Locale

class ModelGenerator(basePackage: String) {

  private val modelPackage = "$basePackage.models"
  private val utilPackage = "$basePackage.util"
  fun generate(spec: OpenAPI): Map<String, FileSpec> {
    return spec.generateComponentSchemaModels() +
      spec.generateComponentResponseModels() +
      spec.generateComponentRequestBodyModels()
  }

  private fun OpenAPI.generateComponentSchemaModels(): Map<String, FileSpec> {
    return components.schemas.mapValues { (name, schema) ->
      FileSpec.builder(modelPackage, name).apply {
        addSchemaType(name, schema)
      }.build()
    }
  }

  private fun OpenAPI.generateComponentResponseModels(): Map<String, FileSpec> {
    return emptyMap()
  }

  private fun OpenAPI.generateComponentRequestBodyModels(): Map<String, FileSpec> {
    return emptyMap()
  }

  private fun FileSpec.Builder.addSchemaType(name: String, schema: Schema<*>) {
    when (schema) {
      is UUIDSchema -> addTypeAlias(TypeAliasSpec.builder(name, Uuid::class).build())
      is DateTimeSchema -> addTypeAlias(TypeAliasSpec.builder(name, String::class).build()) // Needs work
      is IntegerSchema -> addTypeAlias(TypeAliasSpec.builder(name, Int::class).build()) // Needs work
      is NumberSchema -> addTypeAlias(TypeAliasSpec.builder(name, Int::class).build()) // Needs work
      is StringSchema -> addTypeAlias(TypeAliasSpec.builder(name, String::class).build()) // Needs work
      is BooleanSchema -> addTypeAlias(TypeAliasSpec.builder(name, Boolean::class).build())
      else -> addType(schema.toKotlinTypeSpec(name))
    }
  }

  private fun Schema<*>.toKotlinTypeSpec(name: String, parentType: ClassName? = null): TypeSpec {
    val typeName = when (parentType) {
      null -> ClassName(modelPackage, name)
      else -> parentType.nestedClass(name)
    }
    return TypeSpec.classBuilder(name).apply {
      addModifiers(KModifier.DATA)
      addAnnotation(Serializable::class)
      primaryConstructor(
        FunSpec.constructorBuilder().apply {
          addParameters(sanitizedProperties.map { (name, schema) ->
            ParameterSpec.builder(
              name,
              schema.toKotlinTypeName(name, typeName).copy(nullable = name !in requiredProperties)
            ).apply {
              if (schema is UUIDSchema) {
                addAnnotation(AnnotationSpec.builder(Serializable::class).apply {
                  addMember("%T::class", uuidSerializerClassName)
                }.build())
              }
            }.build()
          })
        }.build()
      )

      addProperties(sanitizedProperties.map { (name, schema) ->
        PropertySpec.builder(name, schema.toKotlinTypeName(name, typeName).copy(nullable = name !in requiredProperties))
          .apply {
            initializer(name)
          }.build()
      })

      sanitizedProperties.filterValues { it is ObjectSchema }.forEach { (name, schema) ->
        addType(schema.toKotlinTypeSpec(name = name.capitalized(), parentType = typeName))
      }
    }.build()
  }

  private fun Schema<*>.toKotlinTypeName(propertyName: String, parentType: ClassName): TypeName {
    return when (this) {
      is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName(propertyName, parentType))
      is UUIDSchema -> Uuid::class.asTypeName()
      is DateTimeSchema -> String::class.asTypeName() // todo switch to kotlinx datetime
      is IntegerSchema -> Int::class.asTypeName()
      is NumberSchema -> Int::class.asTypeName()
      is StringSchema -> String::class.asTypeName()
      is BooleanSchema -> Boolean::class.asTypeName()
      is ObjectSchema -> {
        parentType.nestedClass(propertyName.capitalized())
      }

      else -> {
        when {
          isReferenceSchema() -> ClassName(modelPackage, `$ref`.split("/").last())
          else -> error("Unknown schema type: $this")
        }
      }
    }
  }

  private fun Schema<*>.isReferenceSchema(): Boolean = `$ref` != null
  private val Schema<*>.requiredProperties: List<String>
    get() = required ?: emptyList()

  private val Schema<*>.propertiesOrEmpty: Map<String, Schema<*>>
    get() = properties ?: emptyMap()

  private val Schema<*>.sanitizedProperties: Map<String, Schema<*>>
    get() = propertiesOrEmpty.mapKeys { (name, _) ->
      val sanitized = name.sanitizePropertyName()
      if (sanitized.isSnake()) sanitized.snakeToCamel() else sanitized
    }

  private fun emptyObjectType(name: String) = TypeSpec.objectBuilder(name).apply {
    addAnnotation(Serializable::class)
  }.build()

  private fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  private val uuidSerializerClassName: ClassName
    get() = ClassName(utilPackage, "UuidSerializer")

  private fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z]+)+$"))

  private fun String.isCamel() = matches(Regex("^[a-z]+(?:[A-Z][a-z]*)*$"))

  private fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  private fun String.toAngrySnake(): String {
    require(isSnake() || isCamel()) { "The provided string is neither in snake_case nor camelCase." }
    val snakeCaseValue = if (isCamel()) {
      replace(Regex("([A-Z])"), "_$1").lowercase(Locale.getDefault())
    } else {
      this
    }

    return snakeCaseValue.uppercase(Locale.getDefault())
  }

  private fun String.sanitizePropertyName(): String = trim().replace(Regex("\\s+"), "_").lowercase(Locale.getDefault())
}
