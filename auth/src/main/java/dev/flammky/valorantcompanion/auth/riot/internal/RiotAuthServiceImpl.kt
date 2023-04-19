package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.ex.*
import dev.flammky.valorantcompanion.auth.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.auth.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.auth.riot.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

internal class RiotAuthServiceImpl(
    private val repository: RiotAuthRepository
) : RiotAuthService {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val httpClient: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpCookies) {

        }
        install(Auth) {
            bearer {
                sendWithoutRequest { true }
            }
        }
    }

    override fun loginAsync(
        request: RiotLoginRequest
    ): RiotLoginSession {
        val client = RiotLoginClientImpl()
        return client.login(request)
    }
}