package io.bkbn.skribe.codegen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.bkbn.skribe.codegen.generator.ApiClientGenerator
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

@OptIn(ExperimentalPathApi::class)
class OpenApiClientGeneratorTest : DescribeSpec({
  describe("Code Generation") {
    it("Can generate the client for the Neon API") {
      // Act
      val files = ApiClientGenerator.generate(getFileUrl("neon.json"), "tech.neon.client")

      // Assert
      files shouldHaveSize 123
    }
    it("Can generate the client for the Docker Engine API") {
      // Act
      val files = ApiClientGenerator.generate(getFileUrl("docker.yml"), "com.docker.client")

      // Assert
      files shouldHaveSize 248
    }
  }
  describe("Code Compilation") {
    it("Can compile the client code for the Neon API") {
      // Arrange
      val tempDir = createTempDirectory()
      val files = ApiClientGenerator.generate(getFileUrl("neon.json"), "tech.neon.client")
      files.forEach { it.writeTo(tempDir) }
      val sourceFiles = tempDir.walk().filter { it.isRegularFile() }.map { SourceFile.fromPath(it.toFile()) }.toList()
      val compilation = KotlinCompilation().apply {
        sources = sourceFiles
        inheritClassPath = true
        messageOutputStream = System.out
        workingDir = tempDir.toFile()
      }

      // Act
      val result = compilation.compile()

      // Assert
      result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
    it("Can compile the client code for the docker engine API") {
      // Arrange
      val tempDir = createTempDirectory()
      val files = ApiClientGenerator.generate(getFileUrl("docker.yml"), "com.docker.client")
      files.forEach { it.writeTo(tempDir) }
      val sourceFiles = tempDir.walk().filter { it.isRegularFile() }.map { SourceFile.fromPath(it.toFile()) }.toList()
      val compilation = KotlinCompilation().apply {
        sources = sourceFiles
        inheritClassPath = true
        messageOutputStream = System.out
        workingDir = tempDir.toFile()
      }

      // Act
      val result = compilation.compile()

      // Assert
      result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
  }
}) {
  companion object {
    private fun getFileUrl(fileName: String): String =
      this::class.java.classLoader.getResource(fileName)?.toString()
        ?: error("File not found")
  }
}
