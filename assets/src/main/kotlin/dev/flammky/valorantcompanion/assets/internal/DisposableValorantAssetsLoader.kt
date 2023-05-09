package dev.flammky.valorantcompanion.assets.internal

import android.util.Log
import dev.flammky.valorantcompanion.assets.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.ValorantMapAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

internal class DisposableValorantAssetsLoaderClient(
    private val repository: ValorantAssetRepository,
    private val player_card_downloader: PlayerCardAssetDownloader,
    private val map_asset_downloader: ValorantMapAssetDownloader
) : ValorantAssetsLoaderClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun loadUserPlayerCardAsync(req: LoadPlayerCardRequest): Deferred<File> {
        val def = CompletableDeferred<File>(coroutineScope.coroutineContext.job)
        loadUserPlayerCard(req, def)
        return def
    }

    override fun loadMapImageAsync(req: LoadMapImageRequest): Deferred<File> {
        val def = CompletableDeferred<File>(coroutineScope.coroutineContext.job)
        loadMapImage(req, def)
        return def
    }

    override fun dispose() {
        coroutineScope.cancel()
    }

    private fun loadUserPlayerCard(
        req: LoadPlayerCardRequest,
        def: CompletableDeferred<File>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            repository.loadCachedPlayerCard(req.player_card_id, req.acceptableTypes, true)
                .getOrNull()
                ?.let { file ->
                    def.complete(file)
                    return@launch
                }
            // TODO: conflate download
            val art = suspendCancellableCoroutine { cont ->
                player_card_downloader.downloadArt(req.player_card_id, req.acceptableTypes).run {
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
            repository.cachePlayerCard(
                art.id,
                art.type,
                art.barr
            )
            repository.loadCachedPlayerCard(req.player_card_id, req.acceptableTypes, true)
                .onSuccess { file ->
                    file
                        ?.let { def.complete(it) }
                        ?: def.completeExceptionally(CancellationException("repository returned null"))
                }.onFailure {
                    def.completeExceptionally(it)
                }
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
        }
    }

    private fun loadMapImage(
        req: LoadMapImageRequest,
        def: CompletableDeferred<File>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            repository.loadCachedMapImage(req.uuid, req.acceptableTypes, true)
                .getOrNull()
                ?.let { file ->
                    def.complete(file)
                    return@launch
                }
            // TODO: conflate download
            val image = suspendCancellableCoroutine { cont ->
                map_asset_downloader.downloadImage(req.uuid, req.acceptableTypes).run {
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
                .onSuccess { file ->
                    file
                        ?.let { def.complete(it) }
                        ?: def.completeExceptionally(CancellationException("repository returned null"))
                }.onFailure {
                    def.completeExceptionally(it)
                }
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
        }
    }
}