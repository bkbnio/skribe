import io.bkbn.sourdough.clients.model.Account
import io.bkbn.sourdough.clients.request.getAllAccounts
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

suspend fun main() {
  val client = HttpClient(CIO) {
    defaultRequest {
      url("https://broker-api.sandbox.alpaca.markets")
      basicAuth("CK8AP1E2F0ZU6LLU3RU9", "HiYV2RSQ7eFVAvrY7TKuZPLlohs2VrUEdelXL2o6")
    }
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
      })
    }
  }

  val result = client.getAllAccounts()

  println(result.bodyAsText())

  println(result.body<List<Account>>())
}
