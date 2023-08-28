package dev.flammky.valorantcompanion.assets.spray

import dev.flammky.valorantcompanion.assets.map.ValorantMapImage
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*

class ValorantSprayImageDownloadInstance(
    val uuid: String,
    val types: ImmutableSet<ValorantSprayImageType>
) {

    private val def = CompletableDeferred<ValorantSprayImage>()

    fun invokeOnCompletion(
        block: (Throwable?) -> Unit
    ): DisposableHandle = def.invokeOnCompletion(block)

    fun completeExceptionally(
        cause: Throwable
    ): Boolean = def.completeExceptionally(cause)

    fun completeWith(
        result: Result<ValorantSprayImage>
    ): Boolean = def.completeWith(result)

    val isCompleted
        get() = def.isCompleted

    fun getCompleted(): ValorantSprayImage = def.getCompleted()
    fun getCompletedExceptionOrNull(): Throwable? = def.getCompletionExceptionOrNull()

    fun cancel(cancellationException: CancellationException? = null) = def.cancel(cancellationException)
}
