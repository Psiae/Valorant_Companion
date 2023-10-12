package dev.flammky.valorantcompanion.assets.weapon.gunbuddy

import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsChildJob
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.resultingLoop
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*

internal interface ValorantGunBuddyAssetLoader {

    fun loadImageAsync(
        id: String,
        acceptableTypes: ImmutableSet<GunBuddyImageType>
    ): Deferred<Result<LocalImage<*>>>
}

internal class ValorantGunBuddyAssetLoaderImpl(
    private val repository: ValorantAssetRepository,
    private val downloader: ValorantGunBuddyAssetDownloader
): ValorantGunBuddyAssetLoader {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun loadImageAsync(
        id: String,
        acceptableTypes: ImmutableSet<GunBuddyImageType>
    ): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>()

        coroutineScope.launch(Dispatchers.IO) {

            runCatching {
                if (acceptableTypes.isEmpty()) {
                    return@runCatching LocalImage.None
                }

                repository
                    .loadCachedGunBuddyImage(
                        id = id,
                        types = acceptableTypes,
                        awaitAnyWrite = true
                    )
                    .getOrNull()
                    ?.let { file ->
                        return@runCatching LocalImage.File(file)
                    }

                val downloadedImageInstance = downloader
                    .createDownloadInstance(
                        id = id,
                        acceptableTypes = acceptableTypes
                    )
                    .apply {
                        init()
                    }

                resultingLoop<LocalImage<*>> {
                    if (!downloadedImageInstance.hasNext()) {
                        LOOP_BREAK(LocalImage.None)
                    }
                    val offer = downloadedImageInstance
                        .apply {
                            doNext()
                        }
                        .awaitOffer()
                        .getOrElse { ex ->
                            if (BuildConfig.DEBUG) ex.printStackTrace()
                            LOOP_CONTINUE()
                        }
                    repository
                        .cacheGunBuddyImage(
                            id = id,
                            type = offer.type,
                            data = offer.data
                        )
                        .onSuccess {
                            LOOP_BREAK(LocalImage.File(it))
                        }
                }

            }.let { result -> def.complete(result) }

        }.initAsParentCompleter(def)

        return def
    }
}