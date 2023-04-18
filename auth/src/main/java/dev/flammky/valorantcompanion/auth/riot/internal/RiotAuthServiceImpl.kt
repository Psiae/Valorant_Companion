package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.ex.AuthFailureException
import dev.flammky.valorantcompanion.auth.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.auth.ext.getJsonObject
import dev.flammky.valorantcompanion.auth.ext.getJsonObjectOrNull
import dev.flammky.valorantcompanion.auth.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.auth.riot.*
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepositoryImpl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    ): Deferred<RiotLoginRequestResult> = coroutineScope.async(Dispatchers.IO) {

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

        if (obj["error"]?.jsonPrimitive?.toString() == "auth_failure") {
            return@async RiotLoginRequestResult(
                authEx = AuthFailureException()
            )
        }

        val access_token = obj["response"]
            ?.getJsonObjectOrNull("parameters")
            ?.getJsonObjectOrNull("uri")
            ?.jsonPrimitiveOrNull?.toString()?.run {
                drop(indexOf("access_token=") + 1).takeWhile { it != '&' }
            }
            ?: return@async RiotLoginRequestResult(
                responseEx = UnexpectedResponseException()
            )

        RiotLoginRequestResult()
    }
}