package io.bkbn.sourdough.gradle.root

import org.gradle.api.provider.Property
import java.net.URI

abstract class RootExtension {
  abstract val sonatypeNexusUrl: Property<URI>
  abstract val sonatypeSnapshotRepositoryUrl: Property<URI>
  abstract val documentationFolder: Property<String>

  init {
    sonatypeNexusUrl.convention(URI("https://s01.oss.sonatype.org/service/local/"))
    sonatypeSnapshotRepositoryUrl.convention(URI("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    documentationFolder.convention("docs")
  }
}
