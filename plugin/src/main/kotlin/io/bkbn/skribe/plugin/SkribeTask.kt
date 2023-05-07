package io.bkbn.skribe.plugin

import io.bkbn.skribe.codegen.ApiClientGenerator
import io.bkbn.spekt.openapi_3_0.OpenApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class SkribeTask : DefaultTask() {

  private val client = OkHttpClient()

  @get:Input
  abstract val specUrl: Property<String>

  @get:Input
  abstract val outputDir: Property<String>

  @get:Input
  abstract val basePackage: Property<String>

  @get:Input
  @get:Optional
  abstract val shouldCleanDir: Property<Boolean?>

  @TaskAction
  fun generate() {
    logger.quiet("Generating client from ${specUrl.get()} to ${outputDir.get()}")
    val outputDirPath = Path.of(outputDir.get())

    if (shouldCleanDir.orNull == true) {
      logger.quiet("Cleaning directory recursively ${outputDir.get()}")
      outputDirPath.toFile().deleteRecursively()
    }

    val request = Request.Builder()
      .url(specUrl.get())
      .build()
    val response = client.newCall(request).execute()
    val result = response.body?.string() ?: error("No response body")
    val json = Json { ignoreUnknownKeys = true }
    val spec = json.decodeFromString(OpenApi.serializer(), result)

    logger.quiet("Writing files to ${outputDir.get()}")
    val fileSpecs = ApiClientGenerator.generate(spec, basePackage.get())
    fileSpecs.forEach { it.writeTo(outputDirPath) }
  }
}
