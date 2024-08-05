package io.bkbn.skribe.plugin

import io.bkbn.skribe.codegen.utils.StringUtils.capitalized
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.cc.base.logger

class SkribePlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val ext = target.extensions.create("skribe", SkribeExtension::class.java)

      target.tasks.register("skribe", SkribeTask::class.java) {
        it.apis.set(ext.apis)
        it.outputDir.set(ext.outputDir)
        it.shouldCleanDir.set(ext.shouldCleanDir)
      }
    }
}
