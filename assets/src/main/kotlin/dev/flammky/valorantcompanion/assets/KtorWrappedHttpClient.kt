package dev.flammky.valorantcompanion.assets

import io.ktor.client.HttpClient as KtorHttpClient

class KtorWrappedHttpClient(
    val self: KtorHttpClient
) : HttpClient() {

    fun idk() {

    }
}