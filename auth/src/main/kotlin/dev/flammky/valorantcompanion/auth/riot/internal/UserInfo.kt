package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.ex.BadAuthorizationParamException
import dev.flammky.valorantcompanion.auth.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.auth.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.auth.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.auth.riot.UserInfoHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.UserInfoRequestResponseData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal suspend fun retrieveUserInfo(
    httpClient: HttpClient,
    access_token: String,
    session: UserInfoRequestSessionImpl
) {
    val httpRequest = HttpRequestBuilder()
        .apply {
            method = HttpMethod.Get
            url("https://auth.riotgames.com/userinfo")
            headers {
                append("Content-Type", "application/json")
                append("Authorization", "Bearer $access_token")
            }
        }

    val httpResponse = runCatching {
        httpClient.request(httpRequest)
    }.getOrElse { ex ->
        session.onException(ex as Exception)
        return
    }

    val str = runCatching {
        httpResponse.body<String>()
    }.getOrElse { ex ->
        session.onException(ex as Exception)
        return
    }

    val encode = runCatching {
        Json.decodeFromString<JsonElement>(str)
    }.getOrElse { ex ->
        session.onException(ex as Exception)
        return
    }

    session.onResponse(UserInfoHttpRequestResponse(httpResponse.status.value, encode))

    val obj = encode.jsonObjectOrNull

    if (obj == null) {
        if (encode.jsonPrimitiveOrNull?.takeIf { it.isString }?.toString() == "") {
            session.onException(
                BadAuthorizationParamException(null)
            )
        } else {
            session.onException(
                UnexpectedResponseException("")
            )
        }
        return
    }

    val puuid = obj["sub"]
        ?.jsonPrimitiveOrNull
        ?.takeIf { it.isString }
        ?.toString()
        ?.removeSurrounding("\"")

    if (puuid.isNullOrBlank()) {
        session.onException(
            UnexpectedResponseException("sub not found")
        )
        return
    }

    val acct = obj["acct"]
        ?: run {
            session.onException(
                UnexpectedResponseException("acct not found")
            )
            return
        }

    val name = acct.jsonObjectOrNull
        ?.get("game_name")
        ?.jsonPrimitiveOrNull
        ?.takeIf { it.isString }
        ?.toString()
        ?.removeSurrounding("\"")
        ?: run {
            session.onException(
                UnexpectedResponseException("game_name not found")
            )
            return
        }

    val tag = acct.jsonObjectOrNull
        ?.get("tag_line")
        ?.jsonPrimitiveOrNull
        ?.takeIf { it.isString }
        ?.toString()
        ?.removeSurrounding("\"")
        ?: run {
            session.onException(
                UnexpectedResponseException("tag_line not found")
            )
            return
        }

    session.onParse(
        UserInfoRequestResponseData(puuid, name, tag)
    )
}