package dev.flammky.valorantcompanion.assets.weapon.skin

import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.Deferred

abstract class WeaponSkinAssetImageDownloadInstance(
    val id: String,
    val acceptableTypes: PersistentSet<WeaponSkinImageType>
) {

    abstract fun init()

    abstract fun hasNext(): Boolean

    abstract fun doNext(): Boolean

    abstract fun finish()

    abstract fun asDeferred(): Deferred<Result<WeaponSkinRawImage>>

    abstract suspend fun awaitOffer(): Result<WeaponSkinRawImage>
}