package dev.flammky.valorantcompanion.assets.player_title

import kotlinx.coroutines.Deferred

abstract class PlayerTitleAssetDownloadInstance(
    val uuid: String
) {

    abstract fun init()

    abstract suspend fun awaitResult(): Result<ByteArray>

    abstract fun asDeferred(): Deferred<Result<ByteArray>>
}