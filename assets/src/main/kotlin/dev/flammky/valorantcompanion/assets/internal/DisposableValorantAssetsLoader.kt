package dev.flammky.valorantcompanion.assets.internal

import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.bundle.LoadBundleImageRequest
import dev.flammky.valorantcompanion.assets.bundle.ValorantBundleAssetDownloader
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.assets.ex.AssetNotFoundException
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.ValorantMapAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetDownloader
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetIdentity
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.assets.weapon.gunbuddy.GunBuddyImageType
import dev.flammky.valorantcompanion.assets.weapon.gunbuddy.ValorantGunBuddyAssetLoader
import dev.flammky.valorantcompanion.assets.weapon.skin.ValorantWeaponSkinAssetLoader
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinIdentity
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DisposableValorantAssetsLoaderClient(
    private val repository: ValorantAssetRepository,
    private val player_card_downloader: PlayerCardAssetDownloader,
    private val map_asset_downloader: ValorantMapAssetDownloader,
    private val spray_asset_downloader: ValorantSprayAssetDownloader,
    private val bundle_asset_downloader: ValorantBundleAssetDownloader,
    private val gunBuddyAssetLoader: ValorantGunBuddyAssetLoader,
    private val weaponSkinAssetLoader: ValorantWeaponSkinAssetLoader
) : ValorantAssetsLoaderClient {

    private val lifetime = SupervisorJob()
    private val coroutineScope = CoroutineScope(lifetime)

    override fun loadMemoryCachedAgentIcon(agentId: String): LocalImage<*>? {
        // TODO: decide on whether agent icon should be packaged with the app (likely yes)
       return DebugValorantAssetService.AGENT_ICON_MAPPING[agentId]
    }

    override fun loadMemoryCachedRoleIcon(roleId: String): LocalImage<*>? {
        // TODO: decide on whether role icon should be packaged with the app (likely yes)
        return DebugValorantAssetService.ROLE_ICON_MAPPING[roleId]
    }

    override fun loadMemoryCachedCompetitiveRankIcon(rank: CompetitiveRank): LocalImage<*>? {
        // TODO: decide on whether rank icon should be packaged with the app (likely yes)
        return DebugValorantAssetService.COMPETITIVE_RANK_MAPPING[rank]
    }

    override fun loadAgentIconAsync(agentId: String): Deferred<Result<LocalImage<*>>> {
        // TODO: impl
        loadMemoryCachedAgentIcon(agentId)
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(Result.failure(AssetNotFoundException()))
    }

    override fun loadCompetitiveRankIconAsync(rank: CompetitiveRank): Deferred<Result<LocalImage<*>>> {
        loadMemoryCachedCompetitiveRankIcon(rank)
            ?.let {
                return CompletableDeferred(Result.success(it))
            }
        return CompletableDeferred(Result.failure(AssetNotFoundException()))
    }

    override fun loadUserPlayerCardImageAsync(req: LoadPlayerCardRequest): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>(coroutineScope.coroutineContext.job)
        loadUserPlayerCard(req, def)
        return def
    }

    override fun loadMapImageAsync(req: LoadMapImageRequest): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>(coroutineScope.coroutineContext.job)
        loadMapImage(req, def)
        return def
    }

    override fun loadSprayImageAsync(req: LoadSprayImageRequest): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>(coroutineScope.coroutineContext.job)
        loadSprayImage(req, def)
        return def
    }

    override fun loadSprayLevelImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>(coroutineScope.coroutineContext.job)
        loadSprayLevelImage(id, def)
        return def
    }

    override fun loadSprayIdentityAsync(id: String): Deferred<Result<ValorantSprayAssetIdentity>> {
        val def = CompletableDeferred<Result<ValorantSprayAssetIdentity>>(coroutineScope.coroutineContext.job)
        loadSprayIdentity(id, def)
        return def
    }

    override fun loadBundleImageAsync(req: LoadBundleImageRequest): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>(coroutineScope.coroutineContext.job)
        loadBundleImage(req, def)
        return def
    }

    override fun loadCurrencyImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        TODO("Not yet implemented")
    }

    override fun loadWeaponSkinImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        TODO("Not yet implemented")
    }

    override fun loadWeaponSkinTierImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        TODO("Not yet implemented")
    }

    override fun loadWeaponSkinIdentityAsync(id: String): Deferred<Result<WeaponSkinIdentity>> {
        return weaponSkinAssetLoader.loadIdentityAsync(id)
    }

    override fun loadGunBuddyImageAsync(id: String): Deferred<Result<LocalImage<*>>> {
        return gunBuddyAssetLoader.loadImageAsync(id, persistentSetOf(GunBuddyImageType.DISPLAY_ICON))
    }

    override fun dispose() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun loadUserPlayerCard(
        req: LoadPlayerCardRequest,
        def: CompletableDeferred<Result<LocalImage<*>>>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            // TODO: handle exception
            repository.loadCachedPlayerCard(req.uuid, req.acceptableTypes, true)
                .getOrNull()
                ?.let { file ->
                    def.complete(Result.success(LocalImage.File(file)))
                    return@launch
                }
            // TODO: conflate download
            val art = suspendCancellableCoroutine { cont ->
                player_card_downloader.downloadArt(req.uuid, req.acceptableTypes).run {
                    Log.d(
                        BuildConfig.LIBRARY_PACKAGE_NAME,
                        "DisposableValorantAssetLoaderKt: DisposableValorantAssetLoaderClient_loadUserPlayerCard_downloadInstanceRun)"
                    )
                    invokeOnCompletion { t ->
                        Log.d(
                            BuildConfig.LIBRARY_PACKAGE_NAME,
                            "DisposableValorantAssetLoaderKt: DisposableValorantAssetLoaderClient_loadUserPlayerCard_downloadInstanceInvokeOnCompletion($t)"
                        )
                        if (t != null) {
                            cont.resumeWithException(t)
                        } else {
                            cont.resume(requireNotNull(result))
                        }
                    }
                    cont.invokeOnCancellation { ex -> cancel(CancellationException(null, ex)) }
                }
            }
            repository.cachePlayerCard(
                art.id,
                art.type,
                art.barr
            )
            repository.loadCachedPlayerCard(req.uuid, req.acceptableTypes, true)
                .onSuccess { file ->
                    file
                        ?.let { def.complete(Result.success(LocalImage.File(file))) }
                        ?: def.complete(Result.failure(IllegalStateException("repository returned null")))
                }.onFailure {
                    def.complete(Result.failure(it as Exception))
                }
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.cancel(ex as? CancellationException) }
                check(def.isCompleted)
            }
            def.invokeOnCompletion { cancel() }
        }
    }

    private fun loadMapImage(
        req: LoadMapImageRequest,
        def: CompletableDeferred<Result<LocalImage<*>>>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                repository.loadCachedMapImage(req.uuid, req.acceptableTypes, true)
                    .getOrNull()
                    ?.let { file ->
                        return@runCatching file
                    }
                // TODO: conflate download
                val image = suspendCancellableCoroutine { cont ->
                    map_asset_downloader
                        .downloadImage(req.uuid, req.acceptableTypes)
                        .run {
                            invokeOnCompletion { t ->
                                if (t != null) {
                                    cont.resumeWithException(t)
                                } else {
                                    cont.resume(requireNotNull(result))
                                }
                                cont.invokeOnCancellation { ex -> cancel(CancellationException(null, ex)) }
                            }
                        }
                }
                repository.cacheMapImage(
                    id = image.id,
                    type = image.type,
                    data = image.byteArray
                )
                repository.loadCachedMapImage(req.uuid, req.acceptableTypes, true)
                    .getOrElse { ex ->
                        if (ex is CancellationException) throw ex
                        error("repository load gave an error")
                    }
                    ?: error("repository returned null")
            }.onSuccess { file ->
                def.complete(Result.success(LocalImage.File(file)))
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.cancel(ex as? CancellationException) }
            }
            def.invokeOnCompletion { cancel() }
        }
    }

    private fun loadSprayImage(
        req: LoadSprayImageRequest,
        def: CompletableDeferred<Result<LocalImage<*>>>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                repository.loadCachedSprayImage(req.uuid, req.acceptableTypes, true)
                    .getOrNull()
                    ?.let { file ->
                        return@runCatching file
                    }
                // TODO: conflate download
                val image = suspendCancellableCoroutine { cont ->
                    spray_asset_downloader
                        .downloadSprayImage(req.uuid, req.acceptableTypes)
                        .run {
                            invokeOnCompletion { t ->
                                if (t != null) {
                                    cont.resumeWithException(t)
                                } else {
                                    cont.resume(getCompleted())
                                }
                            }
                            cont.invokeOnCancellation { ex -> cancel(CancellationException(null, ex)) }
                        }
                }
                repository.cacheSprayImage(
                    id = image.uuid,
                    type = image.type,
                    data = image.data
                )
                repository.loadCachedSprayImage(req.uuid, req.acceptableTypes, true)
                    .getOrElse { ex ->
                        if (ex is CancellationException) throw ex
                        error("repository load gave an error")
                    }
                    ?: error("repository returned null")
            }.onSuccess { file ->
                def.complete(Result.success(LocalImage.File(file)))
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.cancel(ex as? CancellationException) }
                check(def.isCompleted)
            }
            def.invokeOnCompletion { ex -> cancel(ex as? CancellationException) }
        }
    }

    private fun loadSprayLevelImage(
        uuid: String,
        def: CompletableDeferred<Result<LocalImage<*>>>
    ) {
        val acceptableTypes = persistentSetOf(ValorantSprayImageType.DISPLAY_ICON)
        coroutineScope.launch(Dispatchers.IO) {
            val result = runCatching {
                repository
                    .loadCachedSprayImage(
                        id = uuid,
                        types = acceptableTypes,
                        awaitAnyWrite = true
                    )
                    .getOrNull()
                    ?.let { file -> return@runCatching file }
                val image = suspendCancellableCoroutine { cont ->
                    spray_asset_downloader
                        .downloadSprayLevelImage(uuid)
                        .apply {
                            invokeOnCompletion { t ->
                                if (t != null) {
                                    cont.resumeWithException(t)
                                } else {
                                    cont.resume(getCompleted())
                                }
                            }
                            cont.invokeOnCancellation { ex -> cancel(CancellationException(null, ex)) }
                        }
                }
                repository.cacheSprayImage(
                    id = image.uuid,
                    type = image.type,
                    data = image.data
                )
                repository.loadCachedSprayImage(uuid, acceptableTypes, true)
                    .getOrElse { ex ->
                        if (ex is CancellationException) throw ex
                        error("repository load gave an error")
                    }
                    ?: error("repository returned null")
            }.fold(
                onSuccess = {
                    Result.success(LocalImage.File(it))
                },
                onFailure = {
                    Result.failure(it)
                }
            )
            def.complete(result)
        }.apply {
            initAsParentCompleter(def)
        }
    }

    private fun loadSprayIdentity(
        uuid: String,
        def: CompletableDeferred<Result<ValorantSprayAssetIdentity>>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = runCatching {
                repository
                    .loadCachedSprayIdentity(
                        id = uuid,
                        awaitAnyWrite = true
                    )
                    .getOrNull()
                    ?.let { file -> return@runCatching file }
                val identity = suspendCancellableCoroutine { cont ->
                    spray_asset_downloader
                        .downloadIdentity(uuid)
                        .apply {
                            invokeOnCompletion { t ->
                                if (t != null) {
                                    cont.resumeWithException(t)
                                } else {
                                    cont.resume(getCompleted())
                                }
                            }
                            cont.invokeOnCancellation { ex -> cancel() }
                        }
                }
                repository.cacheSprayIdentity(
                    id = uuid,
                    data = identity
                )
                repository.loadCachedSprayIdentity(
                    uuid,
                    true
                ).getOrElse { ex ->
                    if (ex is CancellationException) throw ex
                    error("repository load gave an error")
                } ?: error("repository returned null")
            }.fold(
                onSuccess = {
                    Result.success(it)
                },
                onFailure = {
                    it.printStackTrace()
                    Result.failure(it)
                }
            )
            def.complete(result)
        }.apply {
            initAsParentCompleter(def)
        }
    }

    private fun loadBundleImage(
        req: LoadBundleImageRequest,
        def: CompletableDeferred<Result<LocalImage<*>>>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val types = req.acceptableTypes

                if (types.isEmpty()) {
                    def.complete(Result.success(LocalImage.Resource(0)))
                    return@launch
                }

                repository
                    .loadCachedBundleImage(
                        id = req.uuid,
                        types = req.acceptableTypes,
                        awaitAnyWrite = true
                    )
                    .getOrNull()?.let { file ->
                        return@runCatching file
                    }

                val image = bundle_asset_downloader
                    .downloadBundleImage(
                        id = req.uuid,
                        acceptableTypes = req.acceptableTypes
                    )
                    .asDeferred()
                    .awaitOrCancelOnException()

                repository
                    .cacheBundleImage(
                        id = req.uuid,
                        type = image.type,
                        data = image.data
                    )
                    .getOrThrow()

            }.fold(
                onSuccess = {
                    Result.success(LocalImage.File(it))
                },
                onFailure = { ex ->
                    if (BuildConfig.DEBUG) ex.printStackTrace()
                    Result.failure(ex)
                }
            )
            def.complete(result)
        }.apply {
            initAsParentCompleter(def)
        }
    }
}