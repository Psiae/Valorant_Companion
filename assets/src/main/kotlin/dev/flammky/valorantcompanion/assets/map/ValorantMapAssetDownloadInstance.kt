package dev.flammky.valorantcompanion.assets.map

import dev.flammky.valorantcompanion.assets.player_card.PlayerCardArt
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class ValorantMapAssetDownloadInstance(
    val id: String,
    val types: ImmutableSet<ValorantMapImageType>
) {
    private val cancellationHandle: Job = Job()
    private val def = CompletableDeferred<ValorantMapImage>()

    fun invokeOnCompletion(
        block: (t: Throwable?) -> Unit
    ) = def.invokeOnCompletion { t -> block(t) }

    fun cancel() {
        cancellationHandle.cancel()
        def.cancel()
    }

    fun completeWith(result: Result<ValorantMapImage>) {
        result.onSuccess {
            def.complete(it)
        }.onFailure { t ->
            def.completeExceptionally(t)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val result: ValorantMapImage?
        get() = if (def.isCompleted && !def.isCancelled) def.getCompleted() else null

    val isCompleted: Boolean
        get() = def.isCompleted

    val exception: Throwable?
        @OptIn(ExperimentalCoroutinesApi::class)
        get() = def.getCompletionExceptionOrNull()

    suspend fun inLifetime(
        block: suspend () -> Unit
        // TODO: find proper solution
    ) = withContext(Job(cancellationHandle)) { block() }
}