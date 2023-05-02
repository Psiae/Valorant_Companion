package dev.flammky.valorantcompanion.pvp.http.ktor

import android.util.Log
import dev.flammky.valorantcompanion.auth.BuildConfig
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.JsonHttpResponse
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import io.ktor.client.HttpClient as KtorHttpClient

internal class KtorWrappedHttpClient() : HttpClient() {

    private val self: KtorHttpClient = KtorHttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpCookies) {

        }
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.i("KtorHttpClient", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    override suspend fun jsonRequest(request: JsonHttpRequest): JsonHttpResponse {
        val ktorResponse = self.request(
            HttpRequestBuilder()
                .apply {
                    method = HttpMethod.parse(request.method)
                    url(request.url)
                    headers {
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        request.headers.forEach { append(it.first, it.second) }
                    }
                    request.body?.let { setBody(it) }
                }
        )
        return JsonHttpResponse(
            ktorResponse.status.value,
            Json.decodeFromString(ktorResponse.bodyAsText())
        )
    }
}