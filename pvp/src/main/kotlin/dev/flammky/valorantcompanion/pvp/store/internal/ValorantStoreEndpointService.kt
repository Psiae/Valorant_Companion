package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.internal.AuthorizationTokens
import dev.flammky.valorantcompanion.pvp.store.ItemType

internal interface ValorantStoreEndpointService {

    val id: String

    fun buildGetStoreFrontDataRequest(
        puuid: String,
        authToken: AuthorizationTokens,
        entitlement: String,
        shard: RiotShard
    ): JsonHttpRequest

    fun buildGetFeaturedBundleDataRequest(
        bundleUUID: String,
    ): JsonHttpRequest

    suspend fun getUserEntitledItems(
        puuid: String,
        httpClient: HttpClient,
        authToken: AuthorizationTokens,
        authEntitlement: String,
        shard: RiotShard,
        itemType: ItemType
    ): Result<Set<String>>
}