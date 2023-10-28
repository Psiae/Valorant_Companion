package dev.flammky.valorantcompanion.assets.player_title

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.valcom.playertitle.ValComPlayerTitleAssetEndpoint
import dev.flammky.valorantcompanion.assets.valorantapi.playertitle.ValorantApiTitleAssetEndpoint

interface PlayerTitleAssetEndpointResolver {

    suspend fun resolveEndpoint(capabilities: Set<String>): PlayerTitleAssetEndpoint?
}

internal class PlayerTitleAssetEndpointResolverImpl(
    httpClientFactory: () -> AssetHttpClient
) : PlayerTitleAssetEndpointResolver {

    private val valorant_api = ValorantApiTitleAssetEndpoint(httpClientFactory)
    private val valcom = ValComPlayerTitleAssetEndpoint(httpClientFactory)

    override suspend fun resolveEndpoint(
        capabilities: Set<String>
    ): PlayerTitleAssetEndpoint? {
        if (valcom.available(capabilities)) {
            return valcom
        }
        if (valorant_api.available(capabilities)) {
            return valorant_api
        }
        return null
    }

    companion object {

    }
}