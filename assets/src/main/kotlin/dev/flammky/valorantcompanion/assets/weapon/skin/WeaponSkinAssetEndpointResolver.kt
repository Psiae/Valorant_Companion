package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.valorantapi.weapon.skin.ValorantApiWeaponSkinAssetEndpoint
import kotlinx.collections.immutable.ImmutableSet

interface WeaponSkinAssetEndpointResolver {

    suspend fun resolveEndpoint(
        excludes: ImmutableSet<String>
    ): WeaponSkinEndpoint?

}

class WeaponSkinAssetEndpointResolverImpl(
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinAssetEndpointResolver {

    private val valorant_api = ValorantApiWeaponSkinAssetEndpoint(
        httpClientFactory = httpClientFactory
    )

    override suspend fun resolveEndpoint(
        excludes: ImmutableSet<String>
    ): WeaponSkinEndpoint? {
        if (valorant_api.ID !in excludes) {
            if (valorant_api.active()) {
                return valorant_api
            }
        }
        return null
    }
}