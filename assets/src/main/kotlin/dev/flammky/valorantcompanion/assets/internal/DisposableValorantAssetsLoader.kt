package dev.flammky.valorantcompanion.assets.internal

import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.ValorantMapAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetDownloader
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DisposableValorantAssetsLoaderClient(
    private val repository: ValorantAssetRepository,
    private val player_card_downloader: PlayerCardAssetDownloader,
    private val map_asset_downloader: ValorantMapAssetDownloader,
    private val spray_asset_downloader: ValorantSprayAssetDownloader
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
        TODO("Not yet implemented")
    }

    override fun loadCompetitiveRankIconAsync(rank: CompetitiveRank): Deferred<Result<LocalImage<*>>> {
        TODO("Not yet implemented")
    }

    override fun loadUserPlayerCardAsync(req: LoadPlayerCardRequest): Deferred<Result<LocalImage<*>>> {
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
                            def.complete(Result.failure(t))
                            cont.resumeWithException(t)
                        } else {
                            cont.resume(requireNotNull(result))
                        }
                    }
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
                                    def.completeExceptionally(t)
                                    cont.cancel(t)
                                } else {
                                    cont.resume(requireNotNull(result))
                                }
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
                        .downloadImage(req.uuid, req.acceptableTypes)
                        .run {
                            invokeOnCompletion { t ->
                                if (t != null) {
                                    def.completeExceptionally(t)
                                    cont.resumeWithException(t)
                                } else {
                                    cont.resume(getCompleted())
                                }
                            }
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
            def.invokeOnCompletion { cancel() }
        }
    }
}