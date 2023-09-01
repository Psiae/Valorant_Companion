package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.internal.AuthorizationTokens

internal class RiotValorantStoreEndpoint : ValorantStoreEndpoint {

    override fun buildGetStoreFrontDataRequest(
        puuid: String,
        authToken: AuthorizationTokens,
        entitlement: String,
        shard: RiotShard
    ): JsonHttpRequest {

        return JsonHttpRequest(
            method = "GET",
            url = "https://pd.${shard.assignedUrlName}.a.pvp.net/store/v2/storefront/$puuid",
            headers = listOf(
                "Authorization" to "Bearer ${authToken.access_token}",
                "X-Riot-Entitlements-JWT" to entitlement
            ),
            body = null
        )
    }
}