package io.bkbn.skribe.codegen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
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
      val files = Skribe.generate(getFileUrl("neon.json"), "tech.neon.client")

      // Assert
      files shouldHaveSize 188
    }
    it("Can generate the client for the Docker Engine API") {
      // Act
      val files = Skribe.generate(getFileUrl("docker.yml"), "com.docker.client")

      // Assert
      files shouldHaveSize 274
    }
    it("Can generate the client for the Alpaca Broker API") {
      // Act
      val files = Skribe.generate(getFileUrl("alpaca-broker.yml"), "com.alpaca.client")

      // Assert
      files shouldHaveSize 181
    }
    it("Can generate the client for the FactSet Prices API") {
      // Act
      val files = Skribe.generate(getFileUrl("factset-prices.yml"), "com.factset.client")

      // Assert
      files shouldHaveSize 93
    }
    it("Can generate the client for the FactSet Fundamentals API") {
      // Act
      val files = Skribe.generate(getFileUrl("factset-fundamentals.yml"), "com.factset.client")

      // Assert
      files shouldHaveSize 202
    }
  }
  describe("Code Compilation") {
    it("Can compile the client code for the Neon API") {
      // Arrange
      val tempDir = createTempDirectory()
      val files = Skribe.generate(getFileUrl("neon.json"), "tech.neon.client")
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
      val files = Skribe.generate(getFileUrl("docker.yml"), "com.docker.client")
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
    it("Can compile the client code for the FactSet Prices API") {
      // Arrange
      val tempDir = createTempDirectory()
      val files = Skribe.generate(getFileUrl("factset-prices.yml"), "com.factset.client")
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
    it("Can compile the client code for the Alpaca Broker API") {
      // Arrange
      val tempDir = createTempDirectory()
      val files = Skribe.generate(getFileUrl("alpaca-broker.yml"), "com.alpaca.client")
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
  describe("Property Manipulation") {
    it("Can attach the proper annotations to a modified enum") {
      // Arrange
      val files = Skribe.generate(getFileUrl("neon.json"), "tech.neon.client")

      // Act
      val content = files.find { it.name == "BranchState" }.toString().trim()

      // Assert
      content shouldBeEqual """
        package tech.neon.client.models

        import kotlinx.serialization.SerialName
        import kotlinx.serialization.Serializable

        @Serializable
        public enum class BranchState {
          @SerialName("init")
          INIT,
          @SerialName("ready")
          READY,
        }
      """.trimIndent()
    }
  }
  describe("Utility Generators") {
    it("Can generate the required serializers") {
      // Arrange
      val files = Skribe.generate(getFileUrl("neon.json"), "tech.neon.client")

      // Act
      val content = files.find { it.name == "Serializers" }.toString().trim()

      // Assert
      content shouldBeEqual """
        package tech.neon.client.util

        import com.benasher44.uuid.Uuid
        import com.benasher44.uuid.uuidFrom
        import java.lang.NumberFormatException
        import kotlin.Number
        import kotlin.Unit
        import kotlinx.serialization.KSerializer
        import kotlinx.serialization.descriptors.PrimitiveKind
        import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
        import kotlinx.serialization.descriptors.SerialDescriptor
        import kotlinx.serialization.encoding.Decoder
        import kotlinx.serialization.encoding.Encoder

        public object UuidSerializer : KSerializer<Uuid> {
          public override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID",
              PrimitiveKind.STRING)

          public override fun deserialize(decoder: Decoder): Uuid = uuidFrom(decoder.decodeString())

          public override fun serialize(encoder: Encoder, `value`: Uuid): Unit {
            encoder.encodeString(value.toString())
          }
        }

        public object NumberSerializer : KSerializer<Number> {
          public override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Number",
              PrimitiveKind.DOUBLE)

          public override fun deserialize(decoder: Decoder): Number = try {
            decoder.decodeDouble()
          } catch (e: NumberFormatException) {
            decoder.decodeInt()
          }

          public override fun serialize(encoder: Encoder, `value`: Number): Unit {
            encoder.encodeString(value.toString())
          }
        }
      """.trimIndent()
    }
  }
}) {
  companion object {
    private fun getFileUrl(fileName: String): String =
      this::class.java.classLoader.getResource(fileName)?.toString()
        ?: error("File not found")
  }
}
