package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.internal.AuthorizationTokens

internal interface ValorantStoreEndpoint {

    fun buildGetStoreFrontDataRequest(
        puuid: String,
        authToken: AuthorizationTokens,
        entitlement: String,
        shard: RiotShard
    ): JsonHttpRequest

    fun buildGetFeaturedBundleDataRequest(
        bundleUUID: String,
    ): JsonHttpRequest
}