package dev.flammky.valorantcompanion.assets.weapon.gunbuddy

import android.util.Log
import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.kt.sync
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.bitByteUnit
import dev.flammky.valorantcompanion.base.storage.toIntCheckNoOverflow
import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

internal interface ValorantGunBuddyAssetDownloader {

    fun createDownloadInstance(
        id: String,
        acceptableTypes: ImmutableSet<GunBuddyImageType>
    ): ValorantGunBuddyImageDownloadInstance
}

internal class ValorantGunBuddyAssetDownloaderImpl(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantGunBuddyAssetEndpoint
): ValorantGunBuddyAssetDownloader {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun createDownloadInstance(
        id: String,
        acceptableTypes: ImmutableSet<GunBuddyImageType>
    ): ValorantGunBuddyImageDownloadInstance {
        return ValorantGunBuddyImageDownloadInstanceImpl(
            id,
            acceptableTypes
        )
    }

    private inner class ValorantGunBuddyImageDownloadInstanceImpl(
        id: String,
        acceptableTypes: ImmutableSet<GunBuddyImageType>
    ): ValorantGunBuddyImageDownloadInstance(id, acceptableTypes) {

        private val supervisor = SupervisorJob()
        private val def = CompletableDeferred<Result<ValorantGunBuddyRawImage>>()

        private val lock = Any()

        @Volatile
        private var downloadCompletion: CompletableDeferred<Result<ValorantGunBuddyRawImage>>? = null

        private var offerIndex = atomic(-1)

        private var initiated = atomic(false)

        @Volatile
        private var offerCont: Continuation<Unit>? = null

        @Volatile
        private var doNext = false

        @Volatile
        private var downloading = false

        @Volatile
        var localInternetConnectionError = false
            private set

        @Volatile
        var uncaughtException: Exception? = null
            private set

        val isCompleted: Boolean
            get() = def.isCompleted

        override fun init() {
            if (!initiated.compareAndSet(expect = false, update = true)) {
                return
            }
            prepare()
            coroutineScope.launch(Dispatchers.IO) {
                Log.d("DEBUG", "GunBuddyAssetDownloader, init_prepare")
                awaitDoNextContinuation()
                Log.d("DEBUG", "GunBuddyAssetDownloader, init_awaitDoNextContinuation")

                if (acceptableTypes.isEmpty()) {
                    downloadStart()
                    downloadEnd(
                        ValorantGunBuddyRawImage(
                            id = id,
                            type = GunBuddyImageType.NONE,
                            data = ByteArray(0)
                        )
                    )
                    return@launch
                }

                acceptableTypes.forEach { type ->
                    downloadStart()
                    downloadImage(id, type)
                    if (hasNext()) {
                        awaitDoNextContinuation()
                        prepareNext()
                    }
                }

                finish()
            }.apply {
                initAsParentCompleter(asDeferred())
            }
        }

        fun downloadEnd(data: ValorantGunBuddyRawImage) {
            Log.d("DEBUG", "GunBuddyAssetDownloader, downloadEnd($data)")
            downloadCompletion
                ?.run {
                    downloading = false
                    complete(Result.success(data))
                }
                ?: error("DownloadCompletion wasn't prepared")
        }

        fun downloadEnd(ex: Exception) {
            Log.d("DEBUG", "GunBuddyAssetDownloader, downloadEnd($ex)")
            downloadCompletion
                ?.run {
                    uncaughtException = ex
                    downloading = false
                    complete(Result.failure(ex))
                }
                ?: error("DownloadCompletion wasn't prepared")
        }

        fun raiseLocalInternetConnectionError() {
            localInternetConnectionError = true
        }

        fun prepare() {
            downloadCompletion = CompletableDeferred()
        }

        fun prepareNext() {
            offerIndex.incrementAndGet()
            downloadCompletion = CompletableDeferred()
        }

        fun downloadStart() {
            downloading = true
        }

        suspend fun awaitDoNextContinuation() {
            val j = Job()
            sync(lock) {
                if (doNext) {
                    doNext = false
                    return
                }
                offerCont = Continuation(EmptyCoroutineContext) { result ->
                    result.fold(
                        onSuccess = { j.complete() },
                        onFailure = { j.completeExceptionally(it) }
                    )
                }
            }
            j.join()
        }

        override fun hasNext(): Boolean {
            return offerIndex.value < acceptableTypes.size -1
        }

        override fun doNext(): Boolean  = sync(lock) {
            if (hasNext() && !downloading && def.isActive) {
                offerCont?.apply { resume(Unit) } ?: run { doNext = true }
                return true
            }
            return false
        }

        override suspend fun awaitOffer(): Result<ValorantGunBuddyRawImage> {
            return downloadCompletion?.await()
                ?: error("DownloadCompletion wasn't prepared")
        }

        override fun finish() {
            downloadCompletion?.let { offer ->
                if (offer.isCompleted) {
                    def.complete(offer.getCompleted())
                    return
                }
            }
            def.cancel()
        }

        private suspend fun downloadImage(
            uuid: String,
            type: GunBuddyImageType,
        ) {
            Log.d("DEBUG", "GunBuddyAssetDownloader, downloadImage($uuid, $type)")
            runCatching {
                val url = endpoint
                    .resolveImageUrlAsync(uuid)
                    .awaitOrCancelOnException()
                    .fold(
                        onSuccess = { it },
                        onFailure = {
                            // TODO: check fail cause
                            return
                        }
                    )

                Log.d("DEBUG", "GunBuddyAssetDownloader, downloadImage($uuid, $type)_url=$url")

                val contentSizeLimit = Companion.IMAGE_DISPLAY_ICON_SIZE_LIMIT
                    .bytes().toIntCheckNoOverflow()

                var result: ByteArray? = null

                assetHttpClient.get(
                    url = url,
                    sessionHandler = handler@ {
                        Log.d("DEBUG", "GunBuddyAssetDownloader, downloadImage($uuid, $type)_sessionHandler")
                        if (
                            contentType != "image"
                        ) return@handler reject()
                        if (
                            contentSubType != "png" &&
                            contentSubType != "jpg" &&
                            contentSubType != "jpeg"
                        ) return@handler reject()

                        val contentLength = contentLength
                        val bb = when {
                            contentLength == null -> return@handler reject()
                            contentLength <= contentSizeLimit -> {
                                ByteBuffer.allocate(contentLength.toInt())
                            }
                            // TODO: ask for confirmation
                            else -> return@handler reject()
                        }
                        consume(bb)
                        result = bb.apply { flip() }.moveToByteArray()
                    }
                )

                result?.let { arr ->
                    downloadEnd(
                        ValorantGunBuddyRawImage(
                            id = uuid,
                            type = type,
                            data = arr
                        )
                    )
                }
            }.onFailure { ex ->
                if (ex is CancellationException) throw ex
                downloadEnd(ex as Exception)
            }
        }

        override fun asDeferred(): Deferred<Result<ValorantGunBuddyRawImage>> {
            return def
        }
    }

    companion object {

        private val IMAGE_DISPLAY_ICON_SIZE_LIMIT: ByteUnit = (128 * 128 * 32).bitByteUnit()
    }
}

