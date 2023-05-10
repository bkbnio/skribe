package io.bkbn.skribe.playground

import io.bkbn.skribe.codegen.ApiClientGenerator

fun main() {
  ApiClientGenerator.generate("https://dfv3qgd2ykmrx.cloudfront.net/api_spec/release/v2.json", "io.bkbn.neon.client")
}
