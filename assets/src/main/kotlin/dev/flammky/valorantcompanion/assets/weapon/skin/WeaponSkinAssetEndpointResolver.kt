package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.valorantapi.weapon.skin.ValorantApiWeaponSkinAssetEndpoint

interface WeaponSkinAssetEndpointResolver {

    suspend fun resolve(): Result<WeaponSkinAssetEndpoint>
}

private class WeaponSkinAssetEndpointResolverImpl(
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinAssetEndpointResolver {

    private val valorant_api = ValorantApiWeaponSkinAssetEndpoint(
        httpClientFactory = httpClientFactory
    )

    override suspend fun resolve(): Result<WeaponSkinAssetEndpoint> {
        if (valorant_api.active()) {
            return Result.success(valorant_api)
        }
        return Result.failure(RuntimeException("No Endpoint were available"))
    }
}