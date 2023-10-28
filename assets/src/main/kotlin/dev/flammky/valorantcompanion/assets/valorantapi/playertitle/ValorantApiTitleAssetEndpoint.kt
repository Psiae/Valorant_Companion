package dev.flammky.valorantcompanion.assets.valorantapi.playertitle

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetEndpoint
import java.io.IOException

class ValorantApiTitleAssetEndpoint(
    httpClientFactory: () -> AssetHttpClient
) : PlayerTitleAssetEndpoint("valorant-api/playertitle") {

    private val _httpClient by lazy(httpClientFactory)

    override suspend fun available(capability: Set<String>): Boolean {
        val httpClient = _httpClient
        var active = false
        capability.forEach {
            if (!CAPABILITIES.contains(it)) {
                return false
            }
        }
        try {
            httpClient.get(
                url = "https://valorant-api.com/v1/version",
                sessionHandler = {
                    if (httpStatusCode == 200) {
                        active = true
                    }
                }
            )
        } catch (io: IOException) {

        }
        return active
    }

    companion object {

        val CAPABILITIES = setOf(
            PlayerTitleAssetEndpoint.CAPABILITY_TITLE_IDENTITY
        )
    }
}