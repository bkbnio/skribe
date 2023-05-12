package io.bkbn.skribe.codegen.generator

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
import io.bkbn.skribe.codegen.utils.SchemaUtils.enumConstants
import io.bkbn.skribe.codegen.utils.SchemaUtils.isReferenceSchema
import io.bkbn.skribe.codegen.utils.SchemaUtils.propertiesOrEmpty
import io.bkbn.skribe.codegen.utils.SchemaUtils.requiredProperties
import io.bkbn.skribe.codegen.utils.StringUtils.capitalized
import io.bkbn.skribe.codegen.utils.StringUtils.convertToCamelCase
import io.bkbn.skribe.codegen.utils.StringUtils.getRefKey
import io.bkbn.skribe.codegen.utils.StringUtils.sanitizeEnumConstant
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BinarySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal sealed interface Generator {
  val basePackage: String
  val openApi: OpenAPI
  val requestPackage
    get() = "$basePackage.requests"
  val modelPackage
    get() = "$basePackage.models"
  val utilPackage
    get() = "$basePackage.util"

  fun Schema<*>.toKotlinTypeSpec(name: String, parentType: ClassName? = null): TypeSpec {
    if (propertiesOrEmpty.isEmpty()) return emptyObjectType(name)
    val typeName = when (parentType) {
      null -> ClassName(modelPackage, name)
      else -> parentType.nestedClass(name)
    }
    return TypeSpec.classBuilder(name).apply {
      addModifiers(KModifier.DATA)
      addAnnotation(Serializable::class)
      createPrimaryConstructor(this@toKotlinTypeSpec, typeName)
      addSchemaProperties(this@toKotlinTypeSpec, typeName)
      attachInlineClasses(this@toKotlinTypeSpec, typeName)
    }.build()
  }

  private fun TypeSpec.Builder.createPrimaryConstructor(schema: Schema<*>, typeName: ClassName) {
    primaryConstructor(
      FunSpec.constructorBuilder().apply {
        addParameters(
          schema.propertiesOrEmpty.map { (propName, propSchema) ->
            val formattedName = propName.convertToCamelCase()
            ParameterSpec.builder(
              formattedName,
              propSchema.toKotlinTypeName(formattedName, typeName).copy(nullable = propName !in schema.requiredProperties)
            ).apply {
              if (propSchema is UUIDSchema) {
                addAnnotation(
                  AnnotationSpec.builder(Serializable::class).apply {
                    addMember("%T::class", uuidSerializerClassName)
                  }.build()
                )
              }
              if (formattedName != propName) {
                addAnnotation(
                  AnnotationSpec.builder(SerialName::class).apply {
                    addMember("%S", propName)
                  }.build()
                )
              }
            }.build()
          }
        )
      }.build()
    )
  }

  private fun TypeSpec.Builder.addSchemaProperties(schema: Schema<*>, typeName: ClassName) {
    addProperties(
      schema.propertiesOrEmpty.map { (propName, propSchema) ->
        val formattedName = propName.convertToCamelCase()
        PropertySpec.builder(
          formattedName,
          propSchema.toKotlinTypeName(formattedName, typeName).copy(nullable = propName !in schema.requiredProperties)
        ).apply {
          initializer(formattedName)
        }.build()
      }
    )
  }

  private fun TypeSpec.Builder.attachInlineClasses(parentSchema: Schema<*>, typeName: ClassName) {
    parentSchema.propertiesOrEmpty.filterValues { it is ObjectSchema || (it is StringSchema && it.enumConstants.isNotEmpty()) }
      .forEach { (name, schema) ->
        val formattedName = name.convertToCamelCase()
        addType(schema.toKotlinTypeSpec(name = formattedName.capitalized(), parentType = typeName))
      }

    parentSchema.propertiesOrEmpty.filterValues { it is ArraySchema && (it.items is ObjectSchema || it.items.enumConstants.isNotEmpty()) }
      .forEach { (name, schema) ->
        val itemSchema = schema.items
        val formattedName = name.convertToCamelCase()
        addType(itemSchema.toKotlinTypeSpec(name = formattedName.capitalized(), parentType = typeName))
      }

    parentSchema.propertiesOrEmpty
      .filterValues {
        it is MapSchema &&
          (it.additionalProperties is ObjectSchema ||
            (it.additionalProperties as Schema<*>).enumConstants.isNotEmpty())
      }
      .forEach { (name, schema) ->
        val additionalPropertySchema = schema.additionalProperties as Schema<*>
        val formattedName = name.convertToCamelCase()
        addType(additionalPropertySchema.toKotlinTypeSpec(name = formattedName.capitalized(), parentType = typeName))
      }
  }

  fun emptyObjectType(name: String) = TypeSpec.objectBuilder(name).apply {
    addAnnotation(Serializable::class)
  }.build()

  val uuidSerializerClassName: ClassName
    get() = ClassName(utilPackage, "UuidSerializer")

  @Suppress("CyclomaticComplexMethod")
  fun Schema<*>.toKotlinTypeName(operationId: String): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName(operationId))
    is UUIDSchema -> Uuid::class.asTypeName()
    is DateTimeSchema -> String::class.asTypeName() // todo switch to kotlinx datetime
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> Int::class.asTypeName()
    is StringSchema -> {
      when {
        enumConstants.isNotEmpty() -> ClassName(modelPackage, operationId.capitalized())
        else -> String::class.asTypeName()
      }
    }

    is ComposedSchema -> ClassName(modelPackage, operationId.capitalized())
    is BooleanSchema -> Boolean::class.asTypeName()
    is ObjectSchema -> ClassName(modelPackage, operationId.capitalized())
    is BinarySchema -> ByteArray::class.asTypeName()
    else -> {
      when {
        isReferenceSchema() -> ClassName(modelPackage, `$ref`.getRefKey())
        else -> error("Unknown schema type: $this")
      }
    }
  }

  @Suppress("CyclomaticComplexMethod")
  fun Schema<*>.toKotlinTypeName(propertyName: String, parentType: ClassName): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName().parameterizedBy(items.toKotlinTypeName(propertyName, parentType))
    is MapSchema -> {
      when (additionalProperties) {
        is Boolean -> Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName())
        is Schema<*> -> Map::class.asTypeName().parameterizedBy(
          String::class.asTypeName(),
          (additionalProperties as Schema<*>).toKotlinTypeName(propertyName, parentType)
        )

        else -> error("Unknown schema type: $this")
      }
    }

    is UUIDSchema -> Uuid::class.asTypeName()
    is DateTimeSchema -> String::class.asTypeName() // todo switch to kotlinx datetime
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> Int::class.asTypeName()
    is StringSchema -> {
      when {
        this.enumConstants.isNotEmpty() -> parentType.nestedClass(propertyName.capitalized())
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
      this@toEnumType.enumConstants
        .filterNot { it == "null" }
        .filterNot { it.isBlank() }
        .forEach { addEnumConstant(it.sanitizeEnumConstant()) }
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
    require(allOf.all { it.`$ref` != null || it is ObjectSchema }) {
      "Currently, all members of allOf must be references or Object Schemas"
    }
    val refs = allOf.map {
      if (it is ObjectSchema) {
        it
      } else {
        openApi.components.schemas[it.`$ref`.getRefKey()]
      }
    }
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
