package dev.flammky.valorantcompanion.assets.weapon.gunbuddy

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.Deferred

abstract class ValorantGunBuddyImageDownloadInstance(
    val id: String,
    val acceptableTypes: ImmutableSet<GunBuddyImageType>
) {

    abstract fun init()

    abstract fun asDeferred(): Deferred<Result<ValorantGunBuddyRawImage>>

    abstract suspend fun awaitOffer(): Result<ValorantGunBuddyRawImage>

    abstract fun hasNext(): Boolean

    abstract fun doNext(): Boolean

    abstract fun finish()
}