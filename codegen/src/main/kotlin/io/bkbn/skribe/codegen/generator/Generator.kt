package io.bkbn.skribe.codegen.generator

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
import io.swagger.v3.oas.models.media.DateSchema
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.EmailSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.media.UUIDSchema
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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
              propSchema.toKotlinTypeName(formattedName, typeName).copy(
                nullable = propName !in schema.requiredProperties
              )
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
              if (propSchema is NumberSchema ||
                openApi.components.schemas?.get(propSchema.`$ref`?.getRefKey()) is NumberSchema
              ) {
                addAnnotation(
                  AnnotationSpec.builder(Serializable::class).apply {
                    val numberSerializerClassName = ClassName(utilPackage, "NumberSerializer")
                    addMember("with = %T::class", numberSerializerClassName)
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
    parentSchema.propertiesOrEmpty
      .filterValues { it is ObjectSchema || (it is StringSchema && it.enumConstants.isNotEmpty()) }
      .forEach { (name, schema) ->
        val formattedName = name.convertToCamelCase()
        addType(schema.toKotlinTypeSpec(name = formattedName.capitalized(), parentType = typeName))
      }

    parentSchema.propertiesOrEmpty
      .filterValues { it is ArraySchema && (it.items is ObjectSchema || it.items.enumConstants.isNotEmpty()) }
      .forEach { (name, schema) ->
        val itemSchema = schema.items
        val formattedName = name.convertToCamelCase()
        addType(itemSchema.toKotlinTypeSpec(name = formattedName.capitalized(), parentType = typeName))
      }

    parentSchema.propertiesOrEmpty
      .filterValues {
        it is MapSchema &&
          (
            it.additionalProperties is ObjectSchema ||
              (it.additionalProperties as Schema<*>).enumConstants.isNotEmpty()
            )
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
    is UUIDSchema -> ClassName("com.benasher44.uuid", "Uuid")
    is DateSchema -> LocalDate::class.asTypeName()
    is DateTimeSchema -> Instant::class.asTypeName()
    is EmailSchema -> String::class.asTypeName()
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> Number::class.asTypeName()
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
  fun Schema<*>.toKotlinTypeName(
    propertyName: String,
    parentType: ClassName,
    attachSerializer: Boolean = false
  ): TypeName = when (this) {
    is ArraySchema -> List::class.asTypeName()
      .parameterizedBy(items.toKotlinTypeName(propertyName, parentType, attachSerializer = true))

    is MapSchema -> {
      when (additionalProperties) {
        is Boolean -> Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName())
        is Schema<*> -> Map::class.asTypeName().parameterizedBy(
          String::class.asTypeName(),
          (additionalProperties as Schema<*>).toKotlinTypeName(propertyName, parentType, attachSerializer = true)
        )

        else -> error("Unknown schema type: $this")
      }
    }

    is UUIDSchema -> when (attachSerializer) {
      false -> ClassName("com.benasher44.uuid", "Uuid")
      true -> ClassName("com.benasher44.uuid", "Uuid").copy(
        annotations = listOf(
          AnnotationSpec.builder(Serializable::class).apply {
            addMember("%T::class", uuidSerializerClassName)
          }.build()
        )
      )
    }
    is DateSchema -> LocalDate::class.asTypeName()
    is DateTimeSchema -> Instant::class.asTypeName()
    is EmailSchema -> String::class.asTypeName()
    is IntegerSchema -> Int::class.asTypeName()
    is NumberSchema -> when (attachSerializer) {
      false -> Number::class.asTypeName()
      true -> Number::class.asTypeName().copy(
        annotations = listOf(
          AnnotationSpec.builder(Serializable::class).apply {
            val numberSerializerClassName = ClassName(utilPackage, "NumberSerializer")
            addMember("with = %T::class", numberSerializerClassName)
          }.build()
        )
      )
    }

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
        .forEach {
          if (it != it.sanitizeEnumConstant()) {
            addEnumConstant(
              it.sanitizeEnumConstant(),
              TypeSpec.anonymousClassBuilder().apply {
                addAnnotation(
                  AnnotationSpec.builder(SerialName::class).apply {
                    addMember("\"$it\"")
                  }.build()
                )
              }.build()
            )
          } else {
            addEnumConstant(it.sanitizeEnumConstant())
          }
        }
    }.build()
  }

  fun ComposedSchema.createComposedKotlinType(name: String): TypeSpec {
    val unifiedSchema = toUnifiedSchema()
    return unifiedSchema.toKotlinTypeSpec(name)
  }

  fun ComposedSchema.toUnifiedSchema(): Schema<*> = when {
    allOf != null -> unifyAllOfSchema()
    anyOf != null -> unifyAnyOfSchema()
    oneOf != null -> unifyOneOfSchema()
    else -> error("Unknown composed schema type: $this")
  }

  fun ComposedSchema.unifyAllOfSchema(): Schema<*> {
    val refs = allOf.map {
      when (it) {
        is ObjectSchema -> it
        is ComposedSchema -> it.toUnifiedSchema()
        else -> when (openApi.components.schemas[it.`$ref`.getRefKey()]) {
          is ObjectSchema -> openApi.components.schemas[it.`$ref`.getRefKey()]
          is ComposedSchema -> (openApi.components.schemas[it.`$ref`.getRefKey()] as ComposedSchema).toUnifiedSchema()
          else -> error("Unknown schema type")
        }
      }
    }
    val objectRefs = refs.map { it as ObjectSchema }
    val gigaSchema = ObjectSchema()
    objectRefs.forEach { it.propertiesOrEmpty.forEach { (name, schema) -> gigaSchema.addProperty(name, schema) } }
    objectRefs.forEach { it.required?.forEach { propName -> gigaSchema.addRequiredItem(propName) } }
    return gigaSchema
  }

  fun ComposedSchema.unifyOneOfSchema(): Schema<*> {
    val refs = oneOf.map {
      when (it) {
        is ObjectSchema -> it
        is ComposedSchema -> it.toUnifiedSchema()
        else -> openApi.components.schemas[it.`$ref`.getRefKey()]
      }
    }
//    val objectRefs = refs.map { it as ObjectSchema }
    val gigaSchema = ObjectSchema()
//    objectRefs.forEach { it.propertiesOrEmpty.forEach { (name, schema) -> gigaSchema.addProperty(name, schema) } }
//    objectRefs.forEach { it.required?.forEach { propName -> gigaSchema.addRequiredItem(propName) } }
    return gigaSchema
  }

  fun ComposedSchema.unifyAnyOfSchema(): Schema<*> {
    val refs = anyOf.map {
      when (it) {
        is ObjectSchema -> it
        is ComposedSchema -> it.toUnifiedSchema()
        else -> openApi.components.schemas[it.`$ref`.getRefKey()]
      }
    }
//    val objectRefs = refs.map { it as ObjectSchema }
    val gigaSchema = ObjectSchema()
//    objectRefs.forEach { it.propertiesOrEmpty.forEach { (name, schema) -> gigaSchema.addProperty(name, schema) } }
//    objectRefs.forEach { it.required?.forEach { propName -> gigaSchema.addRequiredItem(propName) } }
    return gigaSchema
  }

  fun FileSpec.Builder.addSchemaType(name: String, schema: Schema<*>) {
    when (schema) {
      is UUIDSchema -> addTypeAlias(TypeAliasSpec.builder(name, ClassName("com.benasher44.uuid", "Uuid")).build())
      is DateSchema -> addTypeAlias(TypeAliasSpec.builder(name, LocalDate::class).build())
      is DateTimeSchema -> addTypeAlias(TypeAliasSpec.builder(name, Instant::class).build())
      is EmailSchema -> addTypeAlias(TypeAliasSpec.builder(name, String::class).build())
      is IntegerSchema -> addTypeAlias(TypeAliasSpec.builder(name, Int::class).build())
      is NumberSchema -> addTypeAlias(TypeAliasSpec.builder(name, Number::class).build())
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
