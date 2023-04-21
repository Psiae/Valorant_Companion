package dev.flammky.valorantcompanion.assets.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeByteArray
import android.util.Log
import dev.flammky.valorantcompanion.assets.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.conflate.CacheWriteMutex
import dev.flammky.valorantcompanion.assets.conflate.ConflatedCacheWriteMutex
import dev.flammky.valorantcompanion.assets.filesystem.PlatformFileSystem
import dev.flammky.valorantcompanion.assets.sync
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

    private val playerCardFolderPath by lazy {
        with(platformFS) {
            defaultInternalCacheFolder { cache ->
                cache
                    .appendFolder("assets")
                    .appendFolder("player_card")
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
            mutex.write { handle ->
                handle.ensureActive()
                val bitmap = decodeByteArray(data, 0, data.size)
                handle.ensureActive()
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }
            }
            notifyUpdatedProfileCard(fileName, file.absolutePath)
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