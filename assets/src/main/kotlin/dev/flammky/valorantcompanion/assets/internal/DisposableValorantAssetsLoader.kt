package dev.flammky.valorantcompanion.assets.internal

import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import kotlinx.coroutines.*
import java.io.File

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
            player_card_downloader
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex.cause ?: ex) }
        }
    }
}