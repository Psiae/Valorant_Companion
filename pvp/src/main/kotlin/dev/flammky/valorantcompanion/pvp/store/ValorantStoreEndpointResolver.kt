package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.pvp.riot.store.RiotValorantStoreEndpointService
import dev.flammky.valorantcompanion.pvp.store.internal.ValorantStoreEndpointService

internal interface ValorantStoreEndpointResolver {

    suspend fun resolveActiveEndpoint(): Result<ValorantStoreEndpointService>
}

internal class ValorantStoreEndpointResolverImpl: ValorantStoreEndpointResolver {

    override suspend fun resolveActiveEndpoint(): Result<ValorantStoreEndpointService> {
        return Result.success(RiotValorantStoreEndpointService())
    }
}