package dev.flammky.valorantcompanion.pvp.http

internal abstract class HttpClient {

    abstract suspend fun jsonRequest(request: JsonHttpRequest): JsonHttpResponse

    abstract fun dispose()
}

