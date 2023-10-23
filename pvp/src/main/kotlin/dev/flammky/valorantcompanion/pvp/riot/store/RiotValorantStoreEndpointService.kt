package dev.flammky.valorantcompanion.pvp.riot.store

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.base.kt.prefix
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.internal.AuthorizationTokens
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.internal.ValorantStoreEndpointService
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CancellationException

internal class RiotValorantStoreEndpointService : ValorantStoreEndpointService {

    override val id: String
        get() = "riot"

    private val responseParser = RiotValorantStoreResponseParser(

    )

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

    override fun buildGetFeaturedBundleDataRequest(bundleUUID: String): JsonHttpRequest {
        return JsonHttpRequest(
            method = "GET",
            // TODO: dynamic URL
            url = "https://valorant-api.com/v1/bundles/$bundleUUID",
            headers = emptyList(),
            body = null
        )
    }

    override suspend fun getUserEntitledItems(
        puuid: String,
        httpClient: HttpClient,
        authToken: AuthorizationTokens,
        authEntitlement: String,
        shard: RiotShard,
        itemType: ItemType
    ): Result<Set<String>> {
        return runCatching {
            val request = buildHttpGetUserEntitledItemsJsonRequest(
                puuid = puuid,
                shard = shard,
                itemType = itemType,
                authToken = authToken,
                authEntitlement = authEntitlement
            )
            val response = httpClient
                .jsonRequest(request)
            val entitlements = responseParser
                .kotlinxParseGetEntitledItemResponse(
                    response.body.getOrThrow()
                )
                .also { items ->
                    // TODO: check
                }
                .itemUUIDs
            persistentSetOf<String>()
                .builder()
                .apply {
                    add(ValorantAgentIdentity.JETT.uuid)
                    add(ValorantAgentIdentity.PHOENIX.uuid)
                    add(ValorantAgentIdentity.BRIMSTONE.uuid)
                    add(ValorantAgentIdentity.SOVA.uuid)
                    add(ValorantAgentIdentity.SAGE.uuid)
                    addAll(entitlements)
                }
                .build()
        }.onFailure { ex ->
            if (ex is CancellationException) throw ex
        }
    }


    private fun buildHttpGetUserEntitledItemsJsonRequest(
        puuid: String,
        shard: RiotShard,
        itemType: ItemType,
        authToken: AuthorizationTokens,
        authEntitlement: String,
    ): JsonHttpRequest {
        val url = Companion
            .shardBasedBaseUrl(shard)
            .plus(ENTITLEMENT_V1_ENDPOINT_URL.prefix('/'))
            .plus(puuid.prefix('/'))
            .plus(itemType.id.prefix('/'))
        return JsonHttpRequest(
            method = "GET",
            url = url,
            headers = listOf(
                "Authorization" to "Bearer ${authToken.access_token}",
                "X-Riot-Entitlements-JWT" to authEntitlement
            ),
            body = null
        )
    }

    companion object {

        const val ENTITLEMENT_V1_ENDPOINT_URL = "store/v1/entitlements"

        fun shardBasedBaseUrl(
            shard: RiotShard
        ): String {
            return "https://pd.${shard.assignedUrlName}.a.pvp.net"
        }
    }
}