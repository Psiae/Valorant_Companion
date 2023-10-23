package dev.flammky.valorantcompanion.assets.debug

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.agent.ValorantAgentAssetLoader
import dev.flammky.valorantcompanion.assets.bundle.BundleImageIdentifier
import dev.flammky.valorantcompanion.assets.bundle.LoadBundleImageRequest
import dev.flammky.valorantcompanion.assets.ex.AssetNotFoundException
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.MapImageIdentifier
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardIdentifier
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.SprayImageIdentifier
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetIdentity
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinIdentity
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinImageIdentifier
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinImageType
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class DebugValorantAssetsLoaderClient(
    private val agentIconMapping: Map<String, LocalImage<*>>,
    private val roleIconMapping: Map<String, LocalImage<*>>,
    private val competitiveRankIconMapping: Map<CompetitiveRank, LocalImage<*>>,
    private val playerCardMapping: Map<PlayerCardIdentifier, LocalImage<*>>,
    private val mapImageMapping: Map<MapImageIdentifier, LocalImage<*>>,
    private val sprayImageMapping: Map<SprayImageIdentifier, LocalImage<*>>,
    private val sprayIdentityMapping: Map<String, ValorantSprayAssetIdentity>,
    private val bundleImageMapping: Map<BundleImageIdentifier, LocalImage<*>>,
    private val currencyImageMapping: Map<String, LocalImage<*>>,
    private val weaponSkinIdentityMapping: Map<String, WeaponSkinIdentity>,
    private val weaponSkinImageMapping: Map<WeaponSkinImageIdentifier, LocalImage<*>>,
    private val weaponSkinTierImageMapping: Map<String, LocalImage<*>>,
    private val gunBuddyImageMapping: Map<String, LocalImage<*>>
): ValorantAssetsLoaderClient {

    override val agentAssetLoader: ValorantAgentAssetLoader = object : ValorantAgentAssetLoader {

        override fun loadLiveAgentsUUIDsAsync(): Deferred<Result<PersistentSet<String>>> {
            return CompletableDeferred(
                value = runCatching {
                    ValorantAgentIdentity
                        .iterable()
                        .mapTo(persistentSetOf<String>().builder()) { it.uuid }
                        .build()
                }
            )
        }
    }

    override fun loadMemoryCachedAgentIcon(agentId: String): LocalImage<*>? {
        return agentIconMapping[agentId]
    }

    override fun loadMemoryCachedRoleIcon(roleId: String): LocalImage<*>? {
        return roleIconMapping[roleId]
    }

    override fun loadMemoryCachedCompetitiveRankIcon(rank: CompetitiveRank): LocalImage<*>? {
        return competitiveRankIconMapping[rank]
    }

    override fun loadCompetitiveRankIconAsync(rank: CompetitiveRank): Deferred<Result<LocalImage<*>>> {
        return CompletableDeferred(
            value = competitiveRankIconMapping[rank]
                ?.let { Result.success(it) }
                ?: Result.failure(AssetNotFoundException())
        )
    }

    override fun loadAgentIconAsync(agentId: String): Deferred<Result<LocalImage<*>>> {
        return CompletableDeferred(
            value = agentIconMapping[agentId]
                ?.let { Result.success(it) }
                ?: Result.failure(AssetNotFoundException())
        )
    }

    override fun loadAgentRoleIconAsync(roleUUID: String): Deferred<Result<LocalImage<*>>> {
        return CompletableDeferred(
            value = loadMemoryCachedRoleIcon(roleUUID)
                ?.let { Result.success(it) }
                ?: Result.failure(AssetNotFoundException())
        )
    }

    override fun loadUserPlayerCardImageAsync(req: LoadPlayerCardRequest): Deferred<Result<LocalImage<*>>> {
        req.acceptableTypes.forEach { type ->
            playerCardMapping[PlayerCardIdentifier(req.uuid, type)]
                ?.let {
                    return CompletableDeferred(Result.success(it))
                }
        }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadMapImageAsync(req: LoadMapImageRequest): Deferred<Result<LocalImage<*>>> {
        req.acceptableTypes.forEach { type ->
            mapImageMapping[MapImageIdentifier(req.uuid, type)]
                ?.let {
                    return CompletableDeferred(Result.success(it))
                }
        }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadSprayImageAsync(req: LoadSprayImageRequest): Deferred<Result<LocalImage<*>>> {
        req.acceptableTypes.forEach { type ->
            sprayImageMapping[SprayImageIdentifier(req.uuid, type)]
                ?.let {
                    return CompletableDeferred(Result.success(it))
                }
        }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadSprayLevelImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        sprayImageMapping[SprayImageIdentifier(id, ValorantSprayImageType.DISPLAY_ICON)]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadSprayIdentityAsync(id: String): Deferred<Result<ValorantSprayAssetIdentity>> {
        sprayIdentityMapping[id]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadBundleImageAsync(req: LoadBundleImageRequest): Deferred<Result<LocalImage<*>>> {
        req.acceptableTypes.forEach { type ->
            bundleImageMapping[BundleImageIdentifier(req.uuid, type)]
                ?.let {
                    return CompletableDeferred(Result.success(it))
                }
        }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadCurrencyImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        currencyImageMapping[id]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadWeaponSkinImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        weaponSkinImageMapping[WeaponSkinImageIdentifier(id, WeaponSkinImageType.DISPLAY_SMALL)]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadWeaponSkinTierImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        weaponSkinTierImageMapping[id]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadWeaponSkinIdentityAsync(id: String): Deferred<Result<WeaponSkinIdentity>> {
        weaponSkinIdentityMapping[id]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun loadGunBuddyImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        gunBuddyImageMapping[id]
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(
            value = Result.failure(AssetNotFoundException())
        )
    }

    override fun dispose() {

    }
}