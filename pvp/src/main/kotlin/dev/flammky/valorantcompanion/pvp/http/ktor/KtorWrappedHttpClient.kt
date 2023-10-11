package dev.flammky.valorantcompanion.pvp.http.ktor

import android.util.Log
import dev.flammky.valorantcompanion.auth.BuildConfig
import dev.flammky.valorantcompanion.pvp.http.*
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient as KtorHttpClient

internal class KtorWrappedHttpClient(
    lifetime: Job? = null
) : HttpClient() {

    private val self: KtorHttpClient = KtorHttpClient(PVPOkHttpEngineFactory) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpCookies) {

        }
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        val chunked = message
                            .chunked(4000)
                            .joinToString(separator = "\n")
                        Log.i("KtorHttpClient", chunked)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    init {
        lifetime?.invokeOnCompletion { ex ->
            self.cancel(
                CancellationException(
                    message = "Lifetime Completed",
                    cause = ex
                )
            )
        }
    }

    // TODO: rethrow ktor module exceptions
    // TODO: don't return response instead ask for handler
    override suspend fun jsonRequest(request: JsonHttpRequest): JsonHttpResponse {
        val ktorResponse = self.prepareRequest {
            method = HttpMethod.parse(request.method)
            url(request.url)
            headers {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                request.headers.forEach { append(it.first, it.second) }
            }
            request.body?.let { setBody(it) }
        }.execute { response -> response.call.save().response }
        return JsonHttpResponse(
            headers = HttpResponseHeaders(
                ktorResponse.headers.entries().associateByTo(
                    persistentMapOf<String, List<String>>().builder(),
                    keySelector = { entry -> entry.key  },
                    valueTransform = { entry -> entry.value }
                )
            ),
            statusCode = ktorResponse.status.value,
            body = runCatching { Json.decodeFromString(ktorResponse.bodyAsText()) },
            getResponseProperty = { name -> ktorResponse.call.attributes[AttributeKey(name)] }
        )
    }

    override fun dispose() {
        self.close()
    }
}