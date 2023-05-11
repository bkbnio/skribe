package io.bkbn.skribe.codegen

import com.benasher44.uuid.Uuid
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema
import kotlinx.serialization.Serializable
import java.util.Locale

internal sealed interface Generator {
  val basePackage: String
  val openApi: OpenAPI
  val requestPackage
    get() = "$basePackage.requests"
  val modelPackage
    get() = "$basePackage.models"
  val utilPackage
    get() = "$basePackage.util"

  fun String.capitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

  fun String.isSnake() = matches(Regex("^[a-z]+(_[a-z0-9]+)+$"))

  fun String.snakeToCamel() = split("_").mapIndexed { index, word ->
    if (index == 0) word else word.capitalized()
  }.joinToString("")

  fun String.sanitizeEnumConstant(): String =
    trim().replace(Regex("[\\s-]+"), "_").uppercase(Locale.getDefault())

  fun String.sanitizePropertyName(): String = trim().replace(Regex("\\s+"), "_").lowercase(Locale.getDefault())

  val Schema<*>.enumConstants: List<String>
    get() = enum?.map { it.toString() } ?: emptyList()

  fun String.getRefKey() = split("/").last()

  fun Schema<*>.isReferenceSchema(): Boolean = `$ref` != null

  fun Schema<*>.toKotlinTypeSpec(name: String, parentType: ClassName? = null): TypeSpec {
    if (propertiesOrEmpty.isEmpty()) return emptyObjectType(name)
    val typeName = when (parentType) {
      null -> ClassName(modelPackage, name)
      else -> parentType.nestedClass(name)
    }
    return TypeSpec.classBuilder(name).apply {
      addModifiers(KModifier.DATA)
      addAnnotation(Serializable::class)
      primaryConstructor(
        FunSpec.constructorBuilder().apply {
          addParameters(
            sanitizedProperties.map { (name, schema) ->
              ParameterSpec.builder(
                name,
                schema.toKotlinTypeName(name, typeName).copy(nullable = name !in requiredProperties)
              ).apply {
                if (schema is UUIDSchema) {
                  addAnnotation(
                    AnnotationSpec.builder(Serializable::class).apply {
                      addMember("%T::class", uuidSerializerClassName)
                    }.build()
                  )
                }
              }.build()
            }
          )
        }.build()
      )

      addProperties(
        sanitizedProperties.map { (name, schema) ->
          PropertySpec.builder(
            name,
            schema.toKotlinTypeName(name, typeName).copy(nullable = name !in requiredProperties)
          )
            .apply {
              initializer(name)
            }.build()
        }
      )

      sanitizedProperties.filterValues { it is ObjectSchema }.forEach { (name, schema) ->
        addType(schema.toKotlinTypeSpec(name = name.capitalized(), parentType = typeName))
      }
    }.build()
  }

  val Schema<*>.requiredProperties: List<String>
    get() = required ?: emptyList()

  val Schema<*>.propertiesOrEmpty: Map<String, Schema<*>>
    get() = properties ?: emptyMap()

  val Schema<*>.sanitizedProperties: Map<String, Schema<*>>
    get() = propertiesOrEmpty.mapKeys { (name, _) ->
      val sanitized = name.sanitizePropertyName()
      if (sanitized.isSnake()) sanitized.snakeToCamel() else sanitized
    }

  fun emptyObjectType(name: String) = TypeSpec.objectBuilder(name).apply {
    addAnnotation(Serializable::class)
  }.build()

  val uuidSerializerClassName: ClassName
    get() = ClassName(utilPackage, "UuidSerializer")

  fun Schema<*>.toKotlinTypeName(operationId: String): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName(operationId))
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

    is ComposedSchema -> ClassName(modelPackage, operationId.capitalized())
    is BooleanSchema -> Boolean::class.asTypeName()
    else -> {
      when {
        isReferenceSchema() -> ClassName(modelPackage, `$ref`.getRefKey())
        else -> error("Unknown schema type: $this")
      }
    }
  }

  fun Schema<*>.toKotlinTypeName(propertyName: String, parentType: ClassName): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName(propertyName, parentType))
    is UUIDSchema -> Uuid::class.asTypeName()
    is DateTimeSchema -> String::class.asTypeName() // todo switch to kotlinx datetime
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> Int::class.asTypeName()
    is StringSchema -> {
      when {
        enumConstants.isNotEmpty() -> parentType.nestedClass(propertyName.capitalized())
        else -> String::class.asTypeName()
      }
    }

    is BooleanSchema -> Boolean::class.asTypeName()
    is ObjectSchema -> {
      parentType.nestedClass(propertyName.capitalized())
    }

    else -> {
      when {
        isReferenceSchema() -> ClassName(modelPackage, `$ref`.getRefKey())
        else -> error("Unknown schema type: $this")
      }
    }
  }

  fun Schema<*>.toEnumType(name: String): TypeSpec {
    return TypeSpec.enumBuilder(name).apply {
      addAnnotation(Serializable::class)
      this@toEnumType.enumConstants.forEach { addEnumConstant(it.sanitizeEnumConstant()) }
    }.build()
  }

  fun ComposedSchema.createComposedKotlinType(name: String): TypeSpec {
    val unifiedSchema = toUnifiedSchema()
    return unifiedSchema.toKotlinTypeSpec(name)
  }

  fun ComposedSchema.toUnifiedSchema(): Schema<*> = when {
    allOf != null -> unifyAllOfSchema()
    anyOf != null -> TODO()
    oneOf != null -> TODO()
    else -> error("Unknown composed schema type: $this")
  }

  fun ComposedSchema.unifyAllOfSchema(): Schema<*> {
    require(allOf.all { it.`$ref` != null }) { "Currently, all members of allOf must be references" }
    val refs = allOf.map { openApi.components.schemas[it.`$ref`.getRefKey()] }
    require(refs.all { it is ObjectSchema }) { "Currently, all references in an allOf must point to ObjectSchemas" }
    val objectRefs = refs.map { it as ObjectSchema }
    val gigaSchema = ObjectSchema()
    objectRefs.forEach { it.propertiesOrEmpty.forEach { (name, schema) -> gigaSchema.addProperty(name, schema) } }
    objectRefs.forEach { it.required?.forEach { propName -> gigaSchema.addRequiredItem(propName) } }
    return gigaSchema
  }

  fun FileSpec.Builder.addSchemaType(name: String, schema: Schema<*>) {
    when (schema) {
      is UUIDSchema -> addTypeAlias(TypeAliasSpec.builder(name, Uuid::class).build())
      is DateTimeSchema -> addTypeAlias(TypeAliasSpec.builder(name, String::class).build()) // Needs work
      is IntegerSchema -> addTypeAlias(TypeAliasSpec.builder(name, Int::class).build()) // Needs work
      is NumberSchema -> addTypeAlias(TypeAliasSpec.builder(name, Int::class).build()) // Needs work
      is ComposedSchema -> addType(schema.createComposedKotlinType(name))
      is StringSchema -> {
        when {
          schema.enumConstants.isNotEmpty() -> addType(schema.toEnumType(name))
          else -> addTypeAlias(TypeAliasSpec.builder(name, String::class).build())
        }
      }

      is BooleanSchema -> addTypeAlias(TypeAliasSpec.builder(name, Boolean::class).build())
      else -> addType(schema.toKotlinTypeSpec(name))
    }
  }
}