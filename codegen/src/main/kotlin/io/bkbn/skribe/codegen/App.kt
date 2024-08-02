package io.bkbn.skribe.codegen

fun main() {
  Skribe.generate("/alpaca-broker.yml", "com.alpaca.client")
}
