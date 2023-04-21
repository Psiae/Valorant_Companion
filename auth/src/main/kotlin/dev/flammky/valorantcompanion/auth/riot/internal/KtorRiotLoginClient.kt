package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.*
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepositoryImpl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*

internal class KtorRiotLoginClient(
    private val repository: RiotAuthRepositoryImpl
) : RiotLoginClient {

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

    override fun login(
        request: RiotLoginRequest,
        setActive: Boolean
    ): RiotLoginSession {
        val session = RiotLoginSessionImpl()
        val def = CompletableDeferred<Unit>()
        coroutineScope.launch() {
            initiateAuthCookie(httpClient, session.cookie)

            session.cookie.firstException?.let { ex ->
                def.completeExceptionally(ex)
                return@launch
            }

            retrieveAccessToken(httpClient, request.username, request.password, session.auth)

            session.auth.firstException?.let { ex ->
                def.completeExceptionally(ex)
                return@launch
            }

            retrieveEntitlementToken(httpClient, session.auth.data!!.access_token, session.entitlement)

            session.entitlement.firstException?.let { ex ->
                def.completeExceptionally(ex)
                return@launch
            }

            retrieveUserInfo(httpClient, session.auth.data!!.access_token, session.userInfo)

            session.userInfo.firstException?.let { ex ->
                def.completeExceptionally(ex)
                return@launch
            }

            repository.apply {
                registerAuthenticatedAccount(
                    account = run {
                        val data = session.userInfo.data!!
                        RiotAuthenticatedAccount(
                            RiotAccountModel(
                                data.puuid,
                                request.username,
                                data.name,
                                data.tag
                            )
                        )
                    },
                    setActive = setActive
                )
            }
            def.complete(Unit)
        }

        def.invokeOnCompletion {
            session.makeCompleting(it as Exception?)
        }

        return session
    }
}