package dev.flammky.valorantcompanion.assets.valcom.playertitle

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.ktor.KtorHttpClient
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetEndpoint
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.io.IOException

class ValComPlayerTitleAssetEndpoint(
    httpClientFactory: () -> AssetHttpClient
) : PlayerTitleAssetEndpoint("valcom/playertitle") {

    private val _httpClient by lazy(httpClientFactory)

    override suspend fun available(
        capability: Set<String>
    ): Boolean {
        val httpClient = _httpClient
        var available = false
        val params = capability
            .map { key ->
                Companion.CAPABILITIES_MAP[key] ?: return false
            }
            .joinToString(separator = ",")
        try {
            httpClient.get(
                url = if (Companion.LOCALHOST) {
                    val domain = if (LOCALHOST_VM) {
                        // AVD
                        "10.0.2.2"
                    } else {
                        "localhost"
                    }
                    "http://$domain/valorantcompanionapi/assets/playertitles/status?endp=$params"
                } else {
                    "https://valorantcompanionapi.com/assets/playertitles/status?endp=$params"
                },
                sessionHandler = {
                    if (httpStatusCode == 200) {
                        val raw = consumeToByteArray(limit = 10.kiloByteUnit().bytes().toInt())
                        runCatching {
                            Json
                                .parseToJsonElement(String(raw))
                                .expectJsonObject("")
                                .entries.forEach { (k, v) ->
                                    if (!v.expectJsonPrimitive("")
                                            .content
                                            .equals("OK", ignoreCase = true)) {
                                        return@runCatching false
                                    }
                                }
                            available = true
                        }
                    }
                }
            )
        } catch (io: IOException) {

        }
        return available
    }

    companion object {
        val CAPABILITIES = setOf(
            PlayerTitleAssetEndpoint.CAPABILITY_TITLE_IDENTITY,
        )

        val CAPABILITIES_MAP = mapOf(
            PlayerTitleAssetEndpoint.CAPABILITY_TITLE_IDENTITY to "identity"
        )

        const val LOCALHOST = true
        const val LOCALHOST_VM = true
    }
}