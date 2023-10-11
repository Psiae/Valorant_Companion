package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import kotlinx.coroutines.*

internal interface ValorantWeaponSkinAssetLoader {

    fun loadIdentityAsync(id: String): Deferred<Result<WeaponSkinIdentity>>
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
                    ?.let { file -> return@runCatching file }

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
}