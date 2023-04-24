package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.ex.ResponseParsingException
import dev.flammky.valorantcompanion.auth.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.auth.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.auth.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.auth.riot.RegionInfoHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.RegionInfoRequestResponseData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal suspend fun retrieveUserRegion(
    httpClient: HttpClient,
    access_token: String,
    id_token: String,
    session: RegionInfoRequestSessionImpl
) {
    val httpRequest = HttpRequestBuilder()
        .apply {
            method = HttpMethod.Put
            url("https://riot-geo.pas.si.riotgames.com/pas/v1/product/valorant")
            headers {
                append("Content-Type", "application/json")
                append("Authorization", "Bearer $access_token")
            }
            setBody(
                body = buildJsonObject {
                    put("id_token", JsonPrimitive(id_token))
                }
            )
        }

    val httpResponse = runCatching {
        httpClient.request(httpRequest)
    }.onFailure {
        session.onException(it as Exception)
        return
    }.getOrThrow()

    val str = runCatching {
        httpResponse.body<String>()
    }.getOrElse {
        session.onException(it as Exception)
        return
    }

    val obj = runCatching {
        val element = Json.decodeFromString<JsonElement>(str)
        element.jsonObjectOrNull
            ?: run {
                throw UnexpectedResponseException("expected JSON object, but got ${element::class::simpleName} instead")
            }
    }.getOrElse {
        session.onException(it as Exception)
        return
    }

    session.onResponse(
        RegionInfoHttpRequestResponse(httpResponse.status.value, obj)
    )

    val affinities = runCatching {
        obj["affinities"]?.let { obj ->
            obj.jsonObjectOrNull
                ?: throw ResponseParsingException("expected affinities as JSON object, but got ${obj::class::simpleName} instead")
        } ?: run {
            throw ResponseParsingException("affinities not found")
        }
    }.getOrElse {
        session.onException(it as Exception)
        return
    }

    val pbe = runCatching {
        affinities["pbe"]?.let { obj ->
            obj.jsonPrimitiveOrNull
                ?: throw ResponseParsingException("expected pbe as JSON primitive, but got ${obj::class::simpleName} instead")
        } ?: run {
            throw ResponseParsingException("pbe not found")
        }
    }.getOrElse {
        session.onException(it as Exception)
        return
    }

    val live = runCatching {
        affinities["live"]?.let { obj ->
            obj.jsonPrimitiveOrNull
                ?: throw ResponseParsingException("expected live as JSON primitive, but got ${obj::class::simpleName} instead")
        } ?: run {
            throw ResponseParsingException("live not found")
        }
    }.getOrElse {
        session.onException(it as Exception)
        return
    }

    session.onParse(
        RegionInfoRequestResponseData(
            pbe = pbe.toString(),
            live = live.toString()
        )
    )
}