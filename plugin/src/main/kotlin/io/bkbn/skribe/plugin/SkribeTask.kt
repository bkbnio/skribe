package io.bkbn.skribe.plugin

import io.bkbn.skribe.codegen.generator.ApiClientGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class SkribeTask : DefaultTask() {

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

    val fileSpecs = ApiClientGenerator.generate(specUrl.get(), basePackage.get())
    logger.quiet("Writing files to ${outputDir.get()}")
    fileSpecs.forEach { it.writeTo(outputDirPath) }
  }
}
