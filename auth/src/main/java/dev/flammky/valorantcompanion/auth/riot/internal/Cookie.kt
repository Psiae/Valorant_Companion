package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.CookieHttpRequestResponse
import io.ktor.client.HttpClient as KtorHttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal suspend fun initiateAuthCookie(
    httpClient: KtorHttpClient,
    session: CookieRequestSessionImpl
) {

    val httpRequest = HttpRequestBuilder()
        .apply {
            method = HttpMethod.Post
            url("https://auth.riotgames.com/api/v1/authorization")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(buildJsonObject {
                put("client_id", JsonPrimitive("play-valorant-web-prod"))
                put("nonce", JsonPrimitive("1"))
                put("redirect_uri", JsonPrimitive("https://playvalorant.com/opt_in"))
                put("response_type", JsonPrimitive("token id_token"))
                put("scope", JsonPrimitive("account openid"))
            })
        }

    val httpResponse = runCatching {
        httpClient.request(httpRequest)
    }.onFailure { ex ->
        session.onException(ex as Exception)
        return
    }.getOrThrow()

    val element = runCatching {
        val str = httpResponse.bodyAsText()
        Json.decodeFromString<JsonElement>(str)
    }.onFailure { ex ->
        session.onException(ex as Exception)
        return
    }.getOrThrow()

    session.onResponse(
        CookieHttpRequestResponse(
            httpResponse.status.value,
            element
        )
    )
}