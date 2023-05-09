package dev.flammky.valorantcompanion.assets.player_card

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.map.ValorantMapImage
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*
import java.nio.ByteBuffer

class PlayerCardAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantPlayerCardAssetEndpoint
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun downloadArt(
        id: String,
        types: ImmutableSet<PlayerCardArtType>
    ): PlayerCardAssetDownloadInstance {
        return PlayerCardAssetDownloadInstance(id, types)
            .apply {
                initiateDownloadForInstance(this)
            }
    }

    private fun initiateDownloadForInstance(instance: PlayerCardAssetDownloadInstance) {
        coroutineScope.launch(Dispatchers.IO) {
            instance.inLifetime {
                val id = instance.id
                instance.acceptableTypes.forEach { type ->
                    runCatching {
                        // TODO: handle status code
                        val response = assetHttpClient
                            .get(endpoint.buildArtUrl(instance.id, instance.acceptableTypes.first()))
                        // TODO: should be less than 500kb
                        val bb = ByteBuffer.allocate(1_000_000)
                        response.contentChannel.read(bb)
                        instance.completeWith(
                            Result.success(
                                PlayerCardArt(
                                    id = id,
                                    type = type,
                                    bb.apply { flip() }.moveToByteArray()
                                )
                            )
                        )
                        return@inLifetime
                    }
                }
            }
        }.invokeOnCompletion { ex ->
            ex?.let { instance.completeWith(Result.failure(ex)) }
        }
    }
}

class PlayerCardAssetDownloadInstance(
    val id: String,
    val acceptableTypes: ImmutableSet<PlayerCardArtType>
) {

    private val cancellationHandle: Job = Job()
    private val def = CompletableDeferred<PlayerCardArt>()

    fun invokeOnCompletion(
        block: (t: Throwable?) -> Unit
    ) = def.invokeOnCompletion { t -> block(t) }

    fun cancel() {
        cancellationHandle.cancel()
        def.cancel()
    }

    fun completeWith(result: Result<PlayerCardArt>) {
        result.onSuccess {
            def.complete(it)
        }.onFailure { t ->
            def.completeExceptionally(t)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val result: PlayerCardArt?
        get() = if (def.isCompleted && !def.isCancelled) def.getCompleted() else null

    val isCompleted: Boolean
        get() = def.isCompleted

    suspend fun inLifetime(
        block: suspend () -> Unit
        // TODO: find proper solution
    ) = withContext(Job(cancellationHandle)) { block() }
}

class PlayerCardArt(
    val id: String,
    val type: PlayerCardArtType,
    val barr: ByteArray
)