package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.ex.*
import dev.flammky.valorantcompanion.auth.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.auth.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.auth.riot.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

internal class RiotAuthServiceImpl(
    private val httpClient: HttpClient = HttpClient(OkHttp),
    private val repository: RiotAuthRepository
) : RiotAuthService {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun loginAsync(
        request: RiotLoginRequest
    ): RiotLoginSession {
        val session = RiotLoginSessionImpl()
        coroutineScope.launch(Dispatchers.IO) {

            run cookie@ {
                val cookieRequest = HttpRequestBuilder()
                    .apply {
                        method = HttpMethod.Post
                        url("https://auth.riotgames.com/api/v1/authorization")
                        header("Content-Type", "application/json")
                        setBody(
                            body = buildJsonObject {
                                put("client_id", JsonPrimitive("play-valorant-web-prod"))
                                put("nonce", JsonPrimitive("1"))
                                put("redirect_uri", JsonPrimitive("https://playvalorant.com/"))
                                put("response_type", JsonPrimitive("token id_token"))
                                put("scope", JsonPrimitive("account openid"))
                            }
                        )
                    }

                val cookieResponse = httpClient.request(cookieRequest)

                session.provideCookieResponse(
                    cookieResponse.status.value,
                    Json.encodeToJsonElement(cookieResponse.body<String>())
                )
            }

            val accessToken = run auth@ {

                val authRequest = HttpRequestBuilder()
                    .apply {
                        method = HttpMethod.Put
                        url("https://auth.riotgames.com/api/v1/authorization")
                        header("Content-Type", "application/json")
                        setBody(
                            body = buildJsonObject {
                                put("type", JsonPrimitive("auth"))
                                put("username", JsonPrimitive(request.username))
                                put("password", JsonPrimitive(request.password))
                                // TODO
                                put("remember", JsonPrimitive(false))
                                put("language", JsonPrimitive("en_US"))
                            }
                        )
                    }

                val authResponse = httpClient.request(authRequest)
                val obj = Json.encodeToJsonElement(authResponse.body<String>()).jsonObject
                session.provideAuthResponse(authResponse.status.value, obj)

                if (obj["error"]?.jsonPrimitive?.toString() == "auth_failure") {
                    session.authException(
                        AuthFailureException("auth_failure")
                    )
                    throw CancellationException()
                }

                val access_token = obj["response"]
                    ?.jsonObjectOrNull?.get("parameters")
                    ?.jsonObjectOrNull?.get("uri")
                    ?.jsonPrimitiveOrNull
                    ?.takeIf { it.isString }
                    ?.toString()?.run {
                        drop(indexOf("access_token=") + 1).takeWhile { it != '&' }
                    }

                if (access_token == null || access_token.isEmpty()) {
                    session.authException(
                        ResponseParsingException("access_token not found")
                    )
                    throw CancellationException()
                }

            }

            run entitlement@ {
                val entitlementRequest = HttpRequestBuilder()
                    .apply {
                        method = HttpMethod.Post
                        url("https://entitlements.auth.riotgames.com/api/token/v1")
                        headers {
                            append("Content-Type", "application/json")
                            append("Authorization", "Bearer $accessToken")
                        }
                    }

                val entitlementResponse = httpClient.request(entitlementRequest)
                val obj = Json.encodeToJsonElement(entitlementResponse.body<String>()).jsonObject

                session.provideEntitlementResponse(
                    entitlementResponse.status.value,
                    obj
                )

                when(val code = obj["errorCode"]?.jsonPrimitive?.toString()) {
                    null, "" -> Unit
                    "BAD_AUTHORIZATION_PARAM" -> {
                        val msg = obj["message"]?.jsonPrimitive?.toString()
                        val ex = BadAuthorizationParamException(msg)
                        session.entitlementException(ex)
                    }
                    "CREDENTIALS_EXPIRED" -> {
                        val msg = obj["message"]?.jsonPrimitive?.toString()
                        val ex = CredentialExpiredException(msg)
                        session.entitlementException(ex)
                    }
                    else -> error("Unknown Credential Error Code: $code")
                }

                val entitlementToken = obj["entitlements_token"]
                    ?.jsonPrimitiveOrNull
                    ?.takeIf { it.isString }
                    ?.toString()

                if (entitlementToken.isNullOrBlank()) {
                    session.entitlementException(
                        ResponseParsingException("entitlements_token not found")
                    )
                    throw CancellationException()
                }

                session.provideEntitlementData(
                    EntitlementRequestResponseData(entitlementToken)
                )
            }

            run userInfo@ {
                val userInfoRequest = HttpRequestBuilder()
                    .apply {
                        method = HttpMethod.Get
                        url("https://auth.riotgames.com/userinfo")
                        headers {
                            append("Content-Type", "application/json")
                            append("Authorization", "Bearer $accessToken")
                        }
                    }

                val userInfoResponse = httpClient.request(userInfoRequest)
                val str = userInfoResponse.body<String>()
                val encode = Json.encodeToJsonElement(str)
                val obj = encode.jsonObjectOrNull

                session.provideUserInfoResponse(userInfoResponse.status.value, encode)

                if (obj == null) {
                    if (encode.jsonPrimitiveOrNull?.takeIf { it.isString }?.toString() == "") {
                        session.provideUserInfoException(
                            BadAuthorizationParamException(null)
                        )
                    } else {
                        session.provideUserInfoException(
                            UnexpectedResponseException("")
                        )
                    }
                    throw CancellationException()
                }

                val puuid = obj["sub"]
                    ?.jsonPrimitiveOrNull
                    ?.takeIf { it.isString }
                    ?.toString()

                if (puuid.isNullOrBlank()) {
                    session.provideUserInfoException(
                        UnexpectedResponseException("sub not found")
                    )
                    throw CancellationException()
                }

                val acct = obj["acct"]
                    ?: run {
                        session.provideUserInfoException(
                            UnexpectedResponseException("acct not found")
                        )
                        throw CancellationException()
                    }

                val name = acct.jsonObjectOrNull
                    ?.get("game_name")
                    ?.jsonPrimitiveOrNull
                    ?.takeIf { it.isString }
                    ?.toString()
                    ?: run {
                        session.provideUserInfoException(
                            UnexpectedResponseException("game_name not found")
                        )
                        throw CancellationException()
                    }

                val tag = acct.jsonObjectOrNull
                    ?.get("tag_line")
                    ?.jsonPrimitiveOrNull
                    ?.takeIf { it.isString }
                    ?.toString()
                    ?: run {
                        session.provideUserInfoException(
                            UnexpectedResponseException("tag_line not found")
                        )
                        throw CancellationException()
                    }

                session.provideUserInfoData(
                    UserInfoRequestResponseData(puuid, name, tag)
                )
            }

        }.invokeOnCompletion { ex ->
            session.makeCompleting(ex as Exception?)
        }
        return session
    }
}