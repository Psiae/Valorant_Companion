package dev.flammky.valorantcompanion.assets.bundle

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class ValorantBundleImageDownloadInstance(
    val id: String,
    val acceptableTypes: ImmutableSet<BundleImageType>
) {
    private val def = CompletableDeferred<ValorantBundleRawImage>()

    fun complete(
        v: ValorantBundleRawImage
    ): Boolean = def.complete(v)

    fun completeExceptionally(
        ex: Exception
    ): Boolean = def.completeExceptionally(ex)

    fun asDeferred(): Deferred<ValorantBundleRawImage> {
        return def
    }
}