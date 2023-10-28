package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.BuildConfig
import dev.flammky.valorantcompanion.auth.riot.AuthRequestResponseData
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotLoginClient
import dev.flammky.valorantcompanion.base.kt.sync
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*

internal class RiotAuthServiceImpl(
    private val authRepo: RiotAuthRepositoryImpl,
    private val geo: RiotGeoRepositoryImpl
) : RiotAuthService {

    private val jointReauthorizeSessions = mutableMapOf<String, RiotJointReauthorizeSessionImpl>()
    private val coroutineScope = CoroutineScope(SupervisorJob())

    override suspend fun get_entitlement_token(puuid: String): Result<String> {
        return runCatching { authRepo.getEntitlementToken(puuid) ?: TODO() }
    }

    override suspend fun get_authorization(puuid: String): Result<AuthRequestResponseData> {
        return runCatching {
            val access_token = authRepo.getAccessToken(puuid)
            val id_token = authRepo.getIdToken(puuid)
            if (id_token != null && access_token != null) {
                AuthRequestResponseData(access_token, id_token)
            } else TODO()
        }
    }


    override fun createLoginClient(): RiotLoginClient = KtorRiotLoginClient(
        authRepo,
        geo,
        initJointReauthorizeSession = ::initJointReauthorizeSession
    )

    // TODO: reauthorizer
    private fun initJointReauthorizeSession(
        puuid: String
    ): RiotJointReauthorizeSessionImpl {
        val session = jointReauthorizeSessions.sync {
            getOrPut(
                key = puuid,
                defaultValue = { RiotJointReauthorizeSessionImpl() }
            )
        }
        coroutineScope.launch(Dispatchers.IO) {
            session.initiateFlag.join()
            val ssid = authRepo.getSSID(puuid)
                ?: run {
                    session.unknownSSID()
                    session.end()
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
                withContext(session.lifetime) {
                    initiateReAuthCookie(
                        httpClient = httpClient,
                        session = session,
                        ssid = ssid
                    )
                    if (session._reAuthStatusCode.value !in 200 until 300) {
                        return@withContext
                    }
                    retrieveReAuthAccessToken(
                        session = session
                    )
                    if (session._reAuthAccessToken.value == null) {
                        return@withContext
                    }
                    retrieveReAuthEntitlementToken(
                        httpClient = httpClient,
                        session = session
                    )
                    val reAuthAccessToken = session._reAuthAccessToken.value
                        ?: return@withContext
                    val reAuthIdToken = session._reAuthIdToken.value
                        ?: return@withContext
                    val reAuthEntitlementToken = session._entitlementToken.value
                        ?: return@withContext
                    authRepo.apply {
                        updateAccessToken(
                            id = puuid,
                            token = reAuthAccessToken
                        )
                        updateIdToken(
                            id = puuid,
                            token = reAuthIdToken
                        )
                        updateEntitlement(
                            id = puuid,
                            token = reAuthEntitlementToken
                        )
                    }
                    session.success()
                }
            }
            httpClient.close()
        }.invokeOnCompletion {
            jointReauthorizeSessions.sync {
                session.end()
                remove(puuid)
            }
        }

        return session
    }
}