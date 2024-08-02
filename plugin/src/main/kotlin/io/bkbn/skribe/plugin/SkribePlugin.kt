package io.bkbn.skribe.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class SkribePlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val ext = target.extensions.create("skribe", SkribeExtension::class.java)
    target.tasks.register("skribe", SkribeTask::class.java) {
      it.specUrl.set(ext.specUrl)
      it.outputDir.set(ext.outputDir)
      it.basePackage.set(ext.basePackage)
      it.shouldCleanDir.set(ext.shouldCleanDir)
    }
  }
}
