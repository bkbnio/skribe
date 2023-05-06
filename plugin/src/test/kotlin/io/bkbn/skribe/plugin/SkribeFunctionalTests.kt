package io.bkbn.skribe.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SkribeFunctionalTests : DescribeSpec({
  describe("Skribe functional tests") {
    it("can do the thing") {
      // arrange
      val testProjectDir = createTempDirectory()

      val settingsFile = testProjectDir.resolve("settings.gradle.kts")
      settingsFile.writeText("""
        rootProject.name = "skribe-ft"
      """.trimIndent())

      val buildFile = testProjectDir.resolve("build.gradle.kts")
      buildFile.writeKotlin("""
        plugins {
          id("io.bkbn.skribe")
        }

        skribe {
          specUrl.set("https://dfv3qgd2ykmrx.cloudfront.net/api_spec/release/v2.json")
          basePackage.set("io.bkbn.skribe.ft")
          outputDir.set("${'$'}{projectDir}/src/main/kotlin")
        }
      """.trimIndent())

      // act
      val result = GradleRunner.create().apply {
        withProjectDir(testProjectDir.toFile())
        withArguments("skribeGenerate")
        withPluginClasspath()
      }.build()

      // assert
      result.task(":skribeGenerate")?.outcome shouldBe SUCCESS
      print(testProjectDir)
    }
  }
}) {
  companion object {
    private fun Path.writeKotlin(@Language("kotlin") code: String) = writeText(code)
  }
}
