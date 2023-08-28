package dev.flammky.valorantcompanion.assets.spray

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.completeWith

class ValorantSprayIdentityDownloadInstance(
    val uuid: String
) {

    private val def = CompletableDeferred<ValorantSprayAssetIdentity>()

    fun invokeOnCompletion(
        block: (Throwable?) -> Unit
    ): DisposableHandle = def.invokeOnCompletion(block)

    fun completeExceptionally(
        cause: Throwable
    ): Boolean = def.completeExceptionally(cause)

    fun completeWith(
        result: Result<ValorantSprayAssetIdentity>
    ): Boolean = def.completeWith(result)

    val isCompleted
        get() = def.isCompleted

    fun getCompleted(): ValorantSprayAssetIdentity = def.getCompleted()
    fun getCompletedExceptionOrNull(): Throwable? = def.getCompletionExceptionOrNull()

    fun cancel(cancellationException: CancellationException) = def.cancel(cancellationException)
}