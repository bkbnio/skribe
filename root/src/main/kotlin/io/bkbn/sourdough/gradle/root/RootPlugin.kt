package io.bkbn.sourdough.gradle.root

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import kotlinx.kover.KoverPlugin
import kotlinx.kover.api.DefaultJacocoEngine
import kotlinx.kover.api.KoverMergedConfig
import kotlinx.kover.api.KoverProjectConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class RootPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val ext = target.extensions.create("sourdoughRoot", RootExtension::class.java)
    target.configureBaseRepositories()
    target.configureKover()
    target.configureNexus(ext)
  }

  private fun Project.configureBaseRepositories() {
    allprojects {
      it.repositories.apply {
        mavenCentral()
        mavenLocal()
      }
    }
  }

  private fun Project.configureKover() {
    plugins.withType(KoverPlugin::class.java) {
      extensions.configure(KoverProjectConfig::class.java) {
        it.engine.set(DefaultJacocoEngine)
      }
      extensions.configure(KoverMergedConfig::class.java) { kmc ->
        kmc.enable()
        kmc.filters {
          // TODO Add filters when resolved -> https://github.com/Kotlin/kotlinx-kover/issues/220
        }
      }
    }
  }

  private fun Project.configureNexus(ext: RootExtension) {
    plugins.withType(NexusPublishPlugin::class.java) {
      extensions.configure(NexusPublishExtension::class.java) { npe ->
        npe.repositories { nrc ->
          nrc.sonatype { nr ->
            nr.nexusUrl.set(ext.sonatypeNexusUrl)
            nr.snapshotRepositoryUrl.set(ext.sonatypeSnapshotRepositoryUrl)
          }
        }
      }
    }
  }
}
