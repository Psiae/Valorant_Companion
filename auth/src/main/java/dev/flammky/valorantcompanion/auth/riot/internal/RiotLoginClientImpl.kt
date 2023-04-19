package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.RiotLoginClient
import dev.flammky.valorantcompanion.auth.riot.RiotLoginRequest
import dev.flammky.valorantcompanion.auth.riot.RiotLoginSession
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RiotLoginClientImpl() : RiotLoginClient {

    private val httpClient = HttpClient(OkHttp) {
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

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    fun login(
        request: RiotLoginRequest
    ): RiotLoginSession {
        val session = RiotLoginSessionImpl()
        coroutineScope.launch {
            initiateAuthCookieWithKtor(httpClient, session.cookie)

            session.cookie.firstException?.let { return@launch }

            retrieveAccessToken(httpClient, request.username, request.password, session.auth)

            session.auth.firstException?.let { return@launch }

            retrieveEntitlementToken(httpClient, session.auth.data!!.access_token, session.entitlement)

            session.entitlement.firstException?.let { return@launch }



        }.invokeOnCompletion {
            session.makeCompleting(it as Exception?)
        }

        return session
    }
}