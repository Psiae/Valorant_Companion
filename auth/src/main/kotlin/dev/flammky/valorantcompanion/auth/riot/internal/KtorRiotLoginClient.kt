package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.BuildConfig
import dev.flammky.valorantcompanion.auth.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.auth.riot.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*

internal class KtorRiotLoginClient(
    private val auth: RiotAuthRepositoryImpl,
    private val geo: RiotGeoRepositoryImpl
) : RiotLoginClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

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
                        .cookies("auth.riotgames.com")
                        .find { cookie ->
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

        val session = RiotReauthorizeSessionImpl()

        coroutineScope.launch(Dispatchers.IO) {
            val ssid = auth.getSSID(puuid)
                ?: run {
                    session.unknownSSID()
                    return@launch
                }
            val httpClient = HttpClient(OkHttp) {
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
                initiateReAuthCookie(
                    httpClient = httpClient,
                    session = session,
                    ssid = ssid
                )
                if (session._reAuthStatusCode.value !in 200 until 300) {
                    return@runCatching
                }
                retrieveReAuthAccessToken(
                    session = session
                )
                if (session._reAuthAccessToken.value == null) {
                    return@runCatching
                }
                retrieveReAuthEntitlementToken(
                    httpClient = httpClient,
                    session = session
                )
                auth.apply {
                    session._reAuthAccessToken.value?.let { token ->
                        updateAccessToken(
                            id = puuid,
                            token = token
                        )
                    }
                    session._reAuthIdToken.value?.let { token ->
                        updateIdToken(
                            id = puuid,
                            token = token
                        )
                    }
                    session._entitlementToken.value?.let { token ->
                        updateEntitlement(
                            id = puuid,
                            token = token
                        )
                    }
                }
            }
            httpClient.close()
            session.end()
        }

        return session
    }
}