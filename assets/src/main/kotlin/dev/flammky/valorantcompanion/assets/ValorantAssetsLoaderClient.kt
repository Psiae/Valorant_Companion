package dev.flammky.valorantcompanion.assets

import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import kotlinx.coroutines.Deferred
import java.io.File

interface ValorantAssetsLoaderClient {

    fun loadUserPlayerCardAsync(
        req: LoadPlayerCardRequest
    ): Deferred<File>

    fun loadMapImageAsync(
        req: LoadMapImageRequest
    ): Deferred<File>

    fun dispose()
}