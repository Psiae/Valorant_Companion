package dev.flammky.valorantcompanion.assets

import dev.flammky.valorantcompanion.assets.bundle.LoadBundleImageRequest
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetIdentity
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinIdentity
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.coroutines.Deferred

interface ValorantAssetsLoaderClient {

    // TODO: wrap async request to better convey error message

    fun loadMemoryCachedAgentIcon(
        agentId: String
    ): LocalImage<*>?

    fun loadMemoryCachedRoleIcon(
        roleId: String
    ): LocalImage<*>?

    fun loadMemoryCachedCompetitiveRankIcon(
        rank: CompetitiveRank
    ): LocalImage<*>?

    fun loadCompetitiveRankIconAsync(
        rank: CompetitiveRank
    ): Deferred<Result<LocalImage<*>>>

    fun loadAgentIconAsync(
        agentId: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadUserPlayerCardImageAsync(
        req: LoadPlayerCardRequest
    ): Deferred<Result<LocalImage<*>>>

    fun loadMapImageAsync(
        req: LoadMapImageRequest
    ): Deferred<Result<LocalImage<*>>>

    fun loadSprayImageAsync(
        req: LoadSprayImageRequest,
    ): Deferred<Result<LocalImage<*>>>

    fun loadSprayLevelImageAsync(
        id: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadSprayIdentityAsync(
        id: String
    ): Deferred<Result<ValorantSprayAssetIdentity>>

    fun loadBundleImageAsync(
        req: LoadBundleImageRequest
    ): Deferred<Result<LocalImage<*>>>

    fun loadCurrencyImageAsync(
        id: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadWeaponSkinImageAsync(
        id: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadWeaponSkinTierImageAsync(
        id: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadWeaponSkinIdentityAsync(
        id: String
    ): Deferred<Result<WeaponSkinIdentity>>

    fun loadGunBuddyImageAsync(
        id: String
    ): Deferred<Result<LocalImage<*>>>

    fun dispose()
}