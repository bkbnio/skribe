package io.bkbn.skribe.plugin

import io.bkbn.skribe.codegen.Skribe
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class SkribeTask : DefaultTask() {

  @get:Input
  abstract val apis: ListProperty<ApiExtension>

  @get:Input
  abstract val outputDir: Property<String>

  @get:Input
  @get:Optional
  abstract val shouldCleanDir: Property<Boolean?>

  @TaskAction
  fun generate() {
    val outputDirPath = Path.of(outputDir.get())

//    if (shouldCleanDir.orNull == true) {
//      logger.quiet("Cleaning directory recursively ${outputDir.get()}")
//      outputDirPath.toFile().deleteRecursively()
//    }

    apis.get().forEach { api ->
      val fileSpecs = Skribe.generate(api.specUrl, api.basePackage)
      logger.quiet("Writing files to ${outputDir.get()}")
      fileSpecs.forEach { it.writeTo(outputDirPath) }
    }

  }
}
