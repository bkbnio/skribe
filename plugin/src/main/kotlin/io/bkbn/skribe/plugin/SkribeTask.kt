package io.bkbn.skribe.plugin

import io.bkbn.spekt.api.client.codegen.ApiClientGenerator
import io.bkbn.spekt.openapi_3_0.OpenApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
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

  @TaskAction
  fun greet() {
    val request = Request.Builder()
      .url(specUrl.get())
      .build()
    val response = client.newCall(request).execute()
    val result = response.body?.string() ?: error("No response body")
    val json = Json { ignoreUnknownKeys = true }
    val spec = json.decodeFromString(OpenApi.serializer(), result)
    val fileSpecs = ApiClientGenerator.generate(spec, basePackage.get())
    fileSpecs.forEach { it.writeTo(Path.of(outputDir.get())) }
  }
}
