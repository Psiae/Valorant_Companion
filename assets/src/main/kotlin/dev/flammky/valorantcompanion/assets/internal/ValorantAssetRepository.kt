package dev.flammky.valorantcompanion.assets.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.conflate.CacheWriteMutex
import dev.flammky.valorantcompanion.assets.conflate.ConflatedCacheWriteMutex
import dev.flammky.valorantcompanion.assets.filesystem.PlatformFileSystem
import dev.flammky.valorantcompanion.assets.map.ValorantMapImage
import dev.flammky.valorantcompanion.assets.map.ValorantMapImageType
import dev.flammky.valorantcompanion.assets.sync
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import java.io.FileOutputStream

class ValorantAssetRepository(
    private val platformFS: PlatformFileSystem
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val cacheWriteMutexes = mutableMapOf<String, CacheWriteMutex>()
    private val profileCardUpdateChannels = mutableMapOf<String, Channel<String>>()
    private val profileCardUpdateListeners = mutableMapOf<String, MutableList<ProfileCardUpdateListener>>()
    private var profileCardUpdateDispatcher: Job = Job().apply { cancel() }

    //
    // TODO:
    //  I don't think these should be cache's, but rather a downloadable content,
    //  so it should be part of the internal `data` folder,
    //  which we should implement with settings for user to dispose some of it if necessary
    //

    private val playerCardFolderPath by lazy {
        with(platformFS) {
            buildStringWithDefaultInternalCacheFolder { cache ->
                cache
                    .appendFolder("assets")
                    .appendFolder("player_card")
            }
        }
    }

    private val valorantMapFolderPath by lazy {
        with(platformFS) {
            buildStringWithDefaultInternalCacheFolder { cache ->
                cache
                    .appendFolder("assets")
                    .appendFolder("maps")
            }
        }
    }

    suspend fun loadCachedPlayerCard(
        id: String,
        types: PersistentSet<PlayerCardArtType>,
        awaitAnyWrite: Boolean
    ): Result<File?> = runCatching {
        withContext(Dispatchers.IO) {
            types.forEach { type ->
                val fileName = id + "_" + type.name
                with(platformFS) { File(playerCardFolderPath.appendFile(fileName)) }
                    .takeIf {
                        if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                            cacheWriteMutexes[fileName]
                        }?.awaitUnlock()
                        Log.d("ValorantAssetRepository", "loadCachedPlayerCard($id), resolving $it, exist=${it.exists()}")
                        it.exists()
                    }?.let { return@withContext it }
            }
            null
        }
    }

    suspend fun cachePlayerCard(
        id: String,
        type: PlayerCardArtType,
        data: ByteArray
    ): Result<Unit> = runCatching {
        Log.d("ValorantAssetRepository.kt", "cachePlayerCard($id, $type, ${data.size})")
        withContext(Dispatchers.IO) {
            val fileName = id + "_" + type.name
            val file = with(platformFS) {
                File(playerCardFolderPath).mkdirs()
                File(playerCardFolderPath.appendFile(fileName))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut(fileName) { ConflatedCacheWriteMutex() }
            }
            mutex.write { _ ->
                val fileOutputStream = FileOutputStream(file)
                try {
                    fileOutputStream.use { fos ->
                        val lock = fos.channel.lock()
                            ?: error("Could not lock FileChannel")
                        try {
                            val bmp = BitmapFactory
                                .decodeByteArray(data, 0 , data.size)
                                ?: error("Could not decode ByteArray")
                            val out = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            if (!out) error("Could not compress PNG from decoded Bitmap")
                        } finally {
                            lock.release()
                        }
                    }
                } catch (e: Exception) {
                    file.delete()
                    throw e
                }
            }
            notifyUpdatedProfileCard(fileName, file.absolutePath)
        }
    }

    suspend fun cacheMapImage(
        id: String,
        type: ValorantMapImageType,
        data: ByteArray
    ): Result<Unit> = runCatching {
        Log.d("ValorantAssetRepository.kt", "cacheMapImage($id, $type, ${data.size})")
        withContext(Dispatchers.IO) {
            val fileName = id + "_" + type.name
            val file = with(platformFS) {
                File(playerCardFolderPath).mkdirs()
                File(playerCardFolderPath.appendFile(fileName))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut(fileName) { ConflatedCacheWriteMutex() }
            }
            mutex.write { _ ->
                val fileOutputStream = FileOutputStream(file)
                try {
                    fileOutputStream.use { fos ->
                        val lock = fos.channel.lock()
                            ?: error("Could not lock FileChannel")
                        try {
                            val bmp = BitmapFactory
                                .decodeByteArray(data, 0 , data.size)
                                ?: error("Could not decode ByteArray")
                            val out = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            if (!out) error("Could not compress PNG from decoded Bitmap")
                        } finally {
                            lock.release()
                        }
                    }
                } catch (e: Exception) {
                    file.delete()
                    throw e
                }
            }
            // TODO: notify
        }
    }

    suspend fun loadCachedMapImage(
        id: String,
        types: ImmutableSet<ValorantMapImageType>,
        awaitAnyWrite: Boolean
    ): Result<File?> = runCatching {
        withContext(Dispatchers.IO) {
            types.forEach { type ->
                val fileName = id + "_" + type.name
                with(platformFS) { File(playerCardFolderPath.appendFile(fileName)) }
                    .takeIf {
                        if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                            cacheWriteMutexes[fileName]
                        }?.awaitUnlock()
                        Log.d("ValorantAssetRepository", "loadCachedPlayerCard($id), resolving $it, exist=${it.exists()}")
                        it.exists()
                    }?.let { return@withContext it }
            }
            null
        }
    }

    fun registerProfileCardUpdateListener(
        id: String,
        type: PlayerCardArtType,
        listener: ProfileCardUpdateListener
    ) {
        profileCardUpdateListeners.sync {
            val key = id + "_" + type.name
            val list = getOrPut(key) { mutableListOf() }.apply { add(listener) }
            if (list.size == 1) {
                profileCardUpdateChannels.sync {
                    check(put(key, Channel(Channel.CONFLATED)) == null)
                }
                profileCardUpdateDispatcher = initiateProfileCardUpdateDispatcher(id, type)
            }
        }
    }

    fun unregisterProfileCardUpdateListener(
        id: String,
        type: PlayerCardArtType,
        listener: ProfileCardUpdateListener
    ) {
        profileCardUpdateListeners.sync {
            val key = id + "_" + type.name
            val list = get(id + "_" + type.name)?.apply { remove(listener) } ?: return
            if (list.size == 0) {
                profileCardUpdateChannels.sync {
                    checkNotNull(remove(key))
                }
                profileCardUpdateDispatcher.cancel()
            }
        }
    }

    fun initiateProfileCardUpdateDispatcher(
        id: String,
        type: PlayerCardArtType
    ): Job = coroutineScope.launch(Dispatchers.Default) {
        val key = id + "_" + type.name
        profileCardUpdateChannels.sync { get(key)?.receiveAsFlow() }
            ?.collect { path ->
                profileCardUpdateListeners.sync { get(key) }
                    ?.forEach { listener ->
                        listener.onUpdate(path)
                    }
            }
    }

    private fun notifyUpdatedProfileCard(
        fileName: String,
        path: String
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            synchronized(profileCardUpdateChannels) {
                profileCardUpdateChannels[fileName]
            }?.send(path)
        }
    }
}