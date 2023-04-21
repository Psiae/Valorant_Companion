package dev.flammky.valorantcompanion.assets.internal

import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

class DisposableValorantAssetsLoaderClient(
    private val repository: ValorantAssetRepository,
    private val player_card_downloader: PlayerCardAssetDownloader
) : ValorantAssetsLoaderClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun loadUserPlayerCardAsync(req: LoadPlayerCardRequest): Deferred<File> {
        val def = CompletableDeferred<File>(coroutineScope.coroutineContext.job)
        loadUserPlayerCard(req, def)
        return def
    }

    override fun dispose() {}

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
            val art = suspendCancellableCoroutine { cont ->
                player_card_downloader.downloadArt(req.player_card_id, req.acceptableTypes).run {
                    invokeOnCompletion { t ->
                        if (t != null) {
                            def.completeExceptionally(t)
                            cont.cancel(t)
                        }
                        cont.resume(requireNotNull(result))
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
                    file?.let { def.complete(it) } ?: def.completeExceptionally(CancellationException("repository returned null"))
                }.onFailure {
                    def.completeExceptionally(it)
                }
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex.cause ?: ex) }
        }
    }
}