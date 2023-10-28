package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.BuildConfig
import dev.flammky.valorantcompanion.auth.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.auth.riot.*
import dev.flammky.valorantcompanion.base.kt.cast
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*

internal class KtorRiotLoginClient(
    private val auth: RiotAuthRepositoryImpl,
    private val geo: RiotGeoRepositoryImpl,
    private val initJointReauthorizeSession: (puuid: String) -> RiotJointReauthorizeSessionImpl
) : RiotLoginClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    // TODO: check disposal before side-effects
    override fun login(
        request: RiotLoginRequest,
        setActive: Boolean
    ): RiotLoginSession {
        val session = RiotLoginSessionImpl()
        val def = CompletableDeferred<Unit>()
        coroutineScope.launch(Dispatchers.IO) {

            val httpClient = HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json()
                }
                install(HttpCookies) {
                    storage = object : CookiesStorage {
                        val impl = AcceptAllCookiesStorage()
                        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                            Log.d("ValorantCompanion_DEBUG", "requestUrl=$requestUrl, cookie=$cookie")
                            impl.addCookie(requestUrl, cookie)
                        }

                        override fun close() {
                            impl.close()
                        }

                        override suspend fun get(requestUrl: Url): List<Cookie> {
                            return impl.get(requestUrl)
                        }
                    }
                }
                install(Auth) {
                    bearer {
                        sendWithoutRequest { true }
                    }
                }
                if (BuildConfig.DEBUG) {
                    install(Logging) {
                        logger = object : Logger {
                            override fun log(message: String) {
                                Log.i("auth.KtorHttpClient", message)
                            }
                        }
                        level = LogLevel.ALL
                    }
                }
            }

            runCatching {
                initiateAuthCookie(httpClient, session.cookie)

                session.cookie.firstException?.let { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                retrieveAuthAccessToken(httpClient, request.username, request.password, session.auth)

                session.auth.firstException?.let { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                retrieveEntitlementToken(httpClient, session.auth.data!!.access_token, session.entitlement)

                session.entitlement.firstException?.let { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                retrieveUserInfo(httpClient, session.auth.data!!.access_token, session.userInfo)

                session.userInfo.firstException?.let { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                retrieveUserRegion(httpClient, session.auth.data!!.access_token, session.auth.data!!.id_token, session.regionInfo)

                session.regionInfo.firstException?.let { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                val region = runCatching {
                    RiotRegion.resolveByRegionName(session.regionInfo.data!!.live.replace("\"", ""))
                        ?: throw UnexpectedResponseException("Unknown Region: ${session.regionInfo.data!!.live}")
                }.getOrElse { ex ->
                    def.completeExceptionally(ex)
                    return@runCatching
                }

                val shard = RiotShard.ofRegion(region)

                auth.apply {
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
                    val puuid = session.userInfo.data!!.puuid
                    updateAccessToken(
                        puuid,
                        session.auth.data!!.access_token
                    )
                    updateIdToken(
                        puuid,
                        session.auth.data!!.id_token
                    )
                    updateEntitlement(
                        puuid,
                        session.entitlement.data!!.entitlements_token
                    )
                    httpClient
                        .cookies("https://auth.riotgames.com")
                        .find { cookie ->
                            Log.d("ValorantCompanion_DEBUG", "cookie=$cookie")
                            cookie.name == "ssid"
                        }
                        ?.let { cookie ->
                            updateSSID(
                                puuid,
                                cookie.value
                            )
                        }
                }

                geo.apply {
                    updateUserGeoShardInfo(
                        puuid = session.userInfo.data!!.puuid,
                        region = region,
                        shard = shard
                    )
                }
            }

            httpClient.close()
        }

        def.invokeOnCompletion {
            session.makeCompleting(it as Exception?)
        }

        return session
    }

    override fun reauthorize(
        puuid: String
    ): RiotReauthorizeSession {

        val jointSession = initJointReauthorizeSession(
            puuid
        )

        val completion = Job()

        val session = object : RiotReauthorizeSession {

            override val success: Boolean
                get() = !completion.isCancelled && jointSession.success

            override fun asCoroutineJob(): Job {
                return completion
            }
        }

        jointSession
            .apply {
                addWaiter(completion)
                asCoroutineJob().invokeOnCompletion { ex ->
                    if (ex == null) completion.complete() else completion.cancel()
                }
            }

        completion.apply {
           invokeOnCompletion { jointSession.removeWaiter(completion) }
        }

        return session
    }

    override fun dispose() {
        coroutineScope.cancel()
    }
}