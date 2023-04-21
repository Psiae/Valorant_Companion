package dev.flammky.valorantcompanion.assets

import dev.flammky.valorantcompanion.assets.internal.LoadPlayerCardRequest
import kotlinx.coroutines.Deferred
import java.io.File

interface ValorantAssetsLoaderClient {

    fun loadUserPlayerCardAsync(
        req: LoadPlayerCardRequest
    ): Deferred<File>

    fun dispose()
}