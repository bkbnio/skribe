package io.bkbn.skribe.codegen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.bkbn.spekt.openapi_3_0.OpenApi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

@OptIn(ExperimentalPathApi::class)
class OpenApiClientGeneratorTest : DescribeSpec({
  describe("Code Generation") {
    it("Can generate the client for the Neon API") {
      // Arrange
      val spec = json.decodeFromString(OpenApi.serializer(), readFileFromResources("neon.json"))

      // Act (Generate)
      val files = OpenApiClientGenerator("tech.neon.client").generate(spec)

      // Assert
      files shouldHaveSize 116
    }
  }
  describe("Code Compilation") {
    it("Can compile the client code for the Neon API") {
      // Arrange
      val spec = json.decodeFromString(OpenApi.serializer(), readFileFromResources("neon.json"))
      val tempDir = createTempDirectory()
      val files = OpenApiClientGenerator("tech.neon.client").generate(spec)
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
    private val json = Json {
      ignoreUnknownKeys = true
    }

    private fun readFileFromResources(fileName: String): String {
      val snapshotPath = "src/test/resources"
      val file = File("$snapshotPath/$fileName")
      return file.readText()
    }
  }
}
