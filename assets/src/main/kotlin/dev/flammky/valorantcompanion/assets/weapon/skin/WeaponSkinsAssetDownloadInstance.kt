package dev.flammky.valorantcompanion.assets.weapon.skin

import kotlinx.coroutines.Deferred

abstract class WeaponSkinsAssetDownloadInstance {

    abstract fun init()

    abstract suspend fun awaitResult(): Result<ByteArray>

    abstract fun asDeferred(): Deferred<Result<ByteArray>>
}