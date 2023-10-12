package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.resultingLoop
import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.*

internal interface ValorantWeaponSkinAssetLoader {

    fun loadIdentityAsync(id: String): Deferred<Result<WeaponSkinIdentity>>

    fun loadImageAsync(
        id: String,
        acceptableTypes: PersistentSet<WeaponSkinImageType>
    ): Deferred<Result<LocalImage<*>>>
}

internal class ValorantWeaponSkinAssetLoaderImpl(
    private val repository: ValorantAssetRepository,
    private val downloader: ValorantWeaponSkinAssetDownloader
): ValorantWeaponSkinAssetLoader {

    private val coroutineScope = CoroutineScope(SupervisorJob())


    override fun loadIdentityAsync(id: String): Deferred<Result<WeaponSkinIdentity>> {
        val def = CompletableDeferred<Result<WeaponSkinIdentity>>()

        coroutineScope.launch(Dispatchers.IO) {

            val result = runCatching {
                repository
                    .loadCachedWeaponSkinIdentity(
                        id = id,
                        awaitAnyWrite = true
                    )
                    .getOrNull()
                    ?.let { identity -> return@runCatching identity }

                val identity = downloader
                    .createIdentityDownloadInstance(
                        id = id
                    )
                    .apply {
                        init()
                    }
                    .awaitResult()
                    .getOrThrow()

                repository
                    .cacheWeaponSkinIdentity(
                        id = id,
                        data = identity
                    )
                    .getOrThrow()

                repository
                    .loadCachedWeaponSkinIdentity(
                        id = id,
                        awaitAnyWrite = true
                    )
                    .getOrThrow()
                    ?: error("Repository Returned null")
            }

            def.complete(result)

        }.initAsParentCompleter(def)


        return def
    }

    override fun loadImageAsync(
        id: String,
        acceptableTypes: PersistentSet<WeaponSkinImageType>
    ): Deferred<Result<LocalImage<*>>> {
        val def = CompletableDeferred<Result<LocalImage<*>>>()

        coroutineScope.launch(Dispatchers.IO) {

            val result = runCatching {

                if (acceptableTypes.isEmpty()) {
                    return@runCatching LocalImage.None
                }

                repository
                    .loadCachedWeaponSkinImage(
                        id = id,
                        types = acceptableTypes,
                        awaitAnyWrite = true
                    )
                    .getOrNull()
                    ?.let { file -> return@runCatching LocalImage.File(file)  }

                val downloadInstance = downloader
                    .createImageDownloadInstance(
                        id = id,
                        acceptableTypes = acceptableTypes
                    )
                    .apply {
                        init()
                    }

                resultingLoop<LocalImage<*>>() {
                    if (!downloadInstance.hasNext()) {
                        LOOP_BREAK(LocalImage.None)
                    }
                    val offer = downloadInstance
                        .apply {
                            doNext()
                        }
                        .awaitOffer()
                        .getOrElse { LOOP_CONTINUE() }
                    repository
                        .cacheWeaponSkinImage(
                            id = id,
                            type = offer.type,
                            data = offer.data
                        )
                        .onSuccess { file -> LOOP_BREAK(LocalImage.File(file)) }
                }
            }

            def.complete(result)
        }

        return def
    }
}