import com.alpaca.client.broker.model.Account
import com.alpaca.client.broker.request.getAllAccounts
import com.factset.client.prices.request.getSecurityPrices
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val dotenv = dotenv()

suspend fun main() {
  val alpacaClient = HttpClient(CIO) {

    defaultRequest {
      url("https://broker-api.sandbox.alpaca.markets")
      basicAuth(dotenv["ALPACA_USERNAME"], dotenv["ALPACA_PASSWORD"])
    }
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
      })
    }
  }

  val factsetClient = HttpClient(CIO) {
    defaultRequest {
      url("https://api.factset.com/content")
      basicAuth(dotenv["FACTSET_USERNAME"], dotenv["FACTSET_PASSWORD"])
    }
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
      })
    }
  }

  val alpacaResult = alpacaClient.getAllAccounts()
  println(alpacaResult.bodyAsText())
  println(alpacaResult.body<List<Account>>())

  println("-".repeat(50))

  val factsetPriceResult = factsetClient.getSecurityPrices(ids = "AAPL-USA")
  println(factsetPriceResult.bodyAsText())

}
