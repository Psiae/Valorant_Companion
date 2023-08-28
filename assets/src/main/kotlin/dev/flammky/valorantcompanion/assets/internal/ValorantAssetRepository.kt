package dev.flammky.valorantcompanion.assets.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.conflate.CacheWriteMutex
import dev.flammky.valorantcompanion.assets.conflate.ConflatedCacheWriteMutex
import dev.flammky.valorantcompanion.assets.filesystem.PlatformFileSystem
import dev.flammky.valorantcompanion.assets.map.ValorantMapImageType
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardArtType
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetIdentity
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetSerializer
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.base.kt.sync
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import io.ktor.util.cio.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.io.FileOutputStream
import java.io.File as jFile

class ValorantAssetRepository(
    private val platformFS: PlatformFileSystem,
    private val sprayAssetSerializer: ValorantSprayAssetSerializer
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

    private val valorantSprayFolderPath by lazy {
        with(platformFS) {
            buildStringWithDefaultInternalCacheFolder { cache ->
                cache
                    .appendFolder("assets")
                    .appendFolder("sprays")
            }
        }
    }

    private val valorantSprayIdentityFolderPath by lazy {
        with(platformFS) {
            valorantSprayFolderPath.appendFolder("identity")
        }
    }

    suspend fun loadCachedPlayerCard(
        id: String,
        types: PersistentSet<PlayerCardArtType>,
        awaitAnyWrite: Boolean
    ): Result<jFile?> = runCatching {
        withContext(Dispatchers.IO) {
            types.forEach { type ->
                val fileName = id + "_" + type.name
                with(platformFS) { jFile(playerCardFolderPath.appendFile(fileName)) }
                    .takeIf {
                        if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                            cacheWriteMutexes["playerCard_$fileName"]
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
                jFile(playerCardFolderPath).mkdirs()
                jFile(playerCardFolderPath.appendFile(fileName))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut("playerCard_$fileName") { ConflatedCacheWriteMutex() }
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
                jFile(valorantMapFolderPath).mkdirs()
                jFile(valorantMapFolderPath.appendFile(fileName))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut("map_$fileName") { ConflatedCacheWriteMutex() }
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
    ): Result<jFile?> = runCatching {
        withContext(Dispatchers.IO) {
            types.forEach { type ->
                val fileName = id + "_" + type.name
                with(platformFS) { jFile(valorantMapFolderPath.appendFile(fileName)) }
                    .takeIf {
                        if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                            cacheWriteMutexes["map_$fileName"]
                        }?.awaitUnlock()
                        Log.d(
                            "ValorantAssetRepository",
                            "loadCachedMapImage($id), resolving $it, exist=${it.exists()}"
                        )
                        it.exists()
                    }?.let { return@withContext it }
            }
            null
        }
    }

    suspend fun cacheSprayImage(
        id: String,
        type: ValorantSprayImageType,
        data: ByteArray
    ): Result<Unit> = runCatching {
        Log.d("ValorantAssetRepository.kt", "cacheSprayImage($id, $type, ${data.size})")
        withContext(Dispatchers.IO) {
            val fileName = id + "_" + type.name
            val file = with(platformFS) {
                jFile(valorantSprayFolderPath).mkdirs()
                jFile(valorantSprayFolderPath.appendFile(fileName))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut("spray_$fileName") { ConflatedCacheWriteMutex() }
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

    suspend fun loadCachedSprayImage(
        id: String,
        types: ImmutableSet<ValorantSprayImageType>,
        awaitAnyWrite: Boolean
    ): Result<jFile?> = runCatching {
        withContext(Dispatchers.IO) {
            types.forEach { type ->
                val fileName = id + "_" + type.name
                with(platformFS) { jFile(valorantSprayFolderPath.appendFile(fileName)) }
                    .takeIf {
                        if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                            cacheWriteMutexes["spray_$fileName"]
                        }?.awaitUnlock()
                        Log.d(
                            "ValorantAssetRepository",
                            "loadCachedSprayImage($id), resolving $it, exist=${it.exists()}"
                        )
                        it.exists()
                    }?.let { return@withContext it }
            }
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun cacheSprayIdentity(
        id: String,
        data: ValorantSprayAssetIdentity
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val fileName = id
            val folder = jFile(valorantSprayIdentityFolderPath)
            val file = with(platformFS) {
                jFile(valorantSprayIdentityFolderPath.appendFile("v01_$fileName"))
            }
            // use channel and single writer instead ?
            val mutex = synchronized(cacheWriteMutexes) {
                cacheWriteMutexes.getOrPut("spray_identity_v01_$fileName") { ConflatedCacheWriteMutex() }
            }
            Log.d(
                BuildConfig.LIBRARY_PACKAGE_NAME,
                "cacheSprayIdentity($id), file=$file"
            )
            mutex.write { _ ->
                if (file.isDirectory) file.delete()
                folder.mkdirs()
                val fileOutputStream = FileOutputStream(file)
                try {
                    fileOutputStream.use { fos ->
                        val lock = fos.channel.lock()
                            ?: error("Could not lock FileChannel")
                        try {
                            val obj = buildJsonObject {
                                put("uuid", JsonPrimitive(data.uuid))
                                put("displayName", JsonPrimitive(data.displayName))
                                put("category", JsonPrimitive(data.category.codeName))
                                putJsonArray(
                                    key = "levels",
                                    builderAction = {
                                        data.levels.forEach { level ->
                                            addJsonObject {
                                                put("uuid", level.uuid)
                                                put("sprayLevel", level.sprayLevel)
                                                put("displayName", level.displayName)
                                            }
                                        }
                                    }
                                )
                            }
                            Json.encodeToStream(
                                serializer = JsonElement.serializer(),
                                value = obj,
                                stream = fos
                            )
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
    }.apply {
        onFailure { ex ->
            ex.printStackTrace()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun loadCachedSprayIdentity(
        id: String,
        awaitAnyWrite: Boolean
    ): Result<ValorantSprayAssetIdentity?> = runCatching {
        withContext(Dispatchers.IO) {
            val fileName = id
            val file = with(platformFS) {
                jFile(valorantSprayIdentityFolderPath.appendFile("v01_$fileName"))
            }.takeIf { file ->
                if (awaitAnyWrite) synchronized(cacheWriteMutexes) {
                    cacheWriteMutexes["spray_identity_v01_$fileName"]
                }?.awaitUnlock()
                file.exists()
            }
            file?.inputStream()?.use { inStream ->
                if (inStream.available() <= 0 || inStream.available() > 20 * ByteUnit.KB) {
                    //TODO: notify that the said file is unexpected and delete it
                    error("Unexpected File")
                }
                sprayAssetSerializer.deserializeIdentity(
                    uuid = id,
                    raw = inStream.readBytes(),
                    charset = Charsets.UTF_8
                ).getOrThrow()
            }
        }
    }.apply {
        onFailure { ex ->
            ex.printStackTrace()
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