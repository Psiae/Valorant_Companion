package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.kt.sync
import dev.flammky.valorantcompanion.base.storage.*
import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

interface ValorantWeaponSkinAssetDownloader {

    fun createIdentityDownloadInstance(
        id: String
    ): WeaponSkinAssetIdentityDownloadInstance

    fun createImageDownloadInstance(
        id: String,
        acceptableTypes: PersistentSet<WeaponSkinImageType>
    ): WeaponSkinAssetImageDownloadInstance
}


class ValorantWeaponSkinAssetDownloaderImpl(
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient
) : ValorantWeaponSkinAssetDownloader {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val httpClient by lazy(httpClientFactory)

    override fun createIdentityDownloadInstance(
        id: String
    ): WeaponSkinAssetIdentityDownloadInstance {
        return WeaponSkinAssetIdentityDownloadInstanceImpl(
            id = id,
            coroutineScope = coroutineScope,
            endpointResolver = endpointResolver,
            httpClientFactory = ::httpClient
        )
    }

    override fun createImageDownloadInstance(
        id: String,
        acceptableTypes: PersistentSet<WeaponSkinImageType>
    ): WeaponSkinAssetImageDownloadInstance {
        return WeaponSkinImageDownloadInstanceImpl(
            id = id,
            acceptableTypes = acceptableTypes,
            coroutineScope = coroutineScope,
            endpointResolver = endpointResolver,
            httpClientFactory = ::httpClient
        )
    }
}

private class WeaponSkinAssetIdentityDownloadInstanceImpl(
    id: String,
    private val coroutineScope: CoroutineScope,
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinAssetIdentityDownloadInstance(id) {

    private val initiated = atomic(false)

    private val sessionHandlerInvoked = atomic(false)

    private val def = CompletableDeferred<Result<ByteArray>>()

    private val httpClient by lazy {
        check(initiated.value)
        httpClientFactory()
    }

    var noEndpointAvailableError = false
        private set

    override fun init() {
        if (!initiated.compareAndSet(expect = false, update = true)) {
            return
        }
        coroutineScope.launch(Dispatchers.IO) {

            runCatching {
                val endpoint = resolveEndpoint()
                val url = endpoint.buildIdentityUrl(id)

                httpClient.get(
                    url = url,
                    sessionHandler = ::assetSessionHandler
                )

                if (!sessionHandlerInvoked.value) {
                    error("Unexpected Behavior on AssetHttpClient, session handler wasn't invoked")
                }
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }

        }.initAsParentCompleter(asDeferred())
    }

    // TODO: raise error flag
    private suspend fun resolveEndpoint(): WeaponSkinEndpoint {
        return endpointResolver.resolveEndpoint(persistentSetOf())
            ?: noEndpointAvailableError()
    }

    fun noEndpointAvailableError(): Nothing {
        noEndpointAvailableError = true
        throw RuntimeException("No WeaponSkinAssetEndpoint were available")
    }

    private suspend fun assetSessionHandler(
        session: AssetHttpSession
    ) = with(session) {
        if (!sessionHandlerInvoked.compareAndSet(expect = false, update = true)) {
            return
        }
        if (contentType != "application") {
            session.reject()
            return@with
        }
        if (contentSubType != "json") {
            session.reject()
            return@with
        }
        val sizeLimit = Companion.IDENTITY_SIZE_LIMIT
        val contentLength = contentLength
            ?.let { contentLength ->
                if (contentLength.bitByteUnit() > sizeLimit) {
                    session.reject()
                    return@with
                }
                contentLength
            }
        val bb = ByteBuffer.allocate(
            contentLength?.toInt() ?: sizeLimit.toInt()
        )
        consume(bb)
        def.complete(Result.success(bb.apply { flip() }.moveToByteArray()))
    }

    override suspend fun awaitResult(): Result<ByteArray> {
        return def.await()
    }

    override fun asDeferred(): Deferred<Result<ByteArray>> {
        return def
    }

    companion object {
        val IDENTITY_SIZE_LIMIT: ByteUnit = 5.kiloByteUnit()
    }
}

private class WeaponSkinImageDownloadInstanceImpl(
    id: String,
    acceptableTypes: PersistentSet<WeaponSkinImageType>,
    private val coroutineScope: CoroutineScope,
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient,
) : WeaponSkinAssetImageDownloadInstance(
    id,
    acceptableTypes
) {

    private val initiated = atomic(false)

    private val sessionHandlerInvoked = atomic(false)

    private val def = CompletableDeferred<Result<WeaponSkinRawImage>>()

    private val httpClient by lazy {
        check(initiated.value)
        httpClientFactory()
    }

    private val _index = atomic(-1)

    @Volatile
    private var downloading = false

    @Volatile
    private var uncaughtException: Exception? = null

    @Volatile
    private var downloadCompletion: CompletableDeferred<Result<WeaponSkinRawImage>>? = null

    //
    // Guarded by Lock - START
    //

    private val lock = Any()
    private var offerCont: Continuation<Unit>? = null
    private var doNext = false

    //
    // Guarded by Lock - END
    //

    var noEndpointAvailableError = false
        private set

    override fun init() {
        if (!initiated.compareAndSet(expect = false, update = true)) {
            return
        }
        prepare()
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                awaitDoNextContinuation()

                if (acceptableTypes.isEmpty()) {
                    downloadStart()
                    downloadEnd(
                        WeaponSkinRawImage(
                            id = id,
                            type = WeaponSkinImageType.NONE,
                            data = ByteArray(0)
                        )
                    )
                    return@launch
                }

                acceptableTypes.forEach { type ->
                    downloadStart()
                    if (type is WeaponSkinImageType.NONE) {
                        downloadEnd(
                            WeaponSkinRawImage(
                                id = id,
                                type = WeaponSkinImageType.NONE,
                                data = ByteArray(0)
                            )
                        )
                    } else {
                        downloadImage(id, type)
                    }
                    if (hasNext()) {
                        awaitDoNextContinuation()
                        prepareNext()
                    }
                }

                finish()
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }
        }.initAsParentCompleter(asDeferred())
    }

    override fun hasNext(): Boolean {
        return _index.value < acceptableTypes.size - 1
    }

    override fun doNext(): Boolean = sync(lock) {
        if (hasNext() && !downloading && def.isActive) {
            offerCont?.apply { resume(Unit) } ?: run { doNext = true }
            return true
        }
        return false
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

    override suspend fun awaitOffer(): Result<WeaponSkinRawImage> {
        return downloadCompletion?.await()
            ?: error("DownloadCompletion wasn't prepared")
    }

    override fun asDeferred(): Deferred<Result<WeaponSkinRawImage>> {
        return def
    }

    // TODO: raise error flag
    private suspend fun resolveEndpoint(): WeaponSkinEndpoint {
        return endpointResolver.resolveEndpoint(persistentSetOf())
            ?: noEndpointAvailableError()
    }

    fun prepare() {
        downloadCompletion = CompletableDeferred()
    }

    fun prepareNext() {
        _index.incrementAndGet()
        downloadCompletion = CompletableDeferred()
        sessionHandlerInvoked.value = false
    }

    fun downloadStart() {
        downloading = true
    }

    fun downloadEnd(data: WeaponSkinRawImage) {
        downloadCompletion
            ?.run {
                downloading = false
                complete(Result.success(data))
            }
            ?: error("DownloadCompletion wasn't prepared")
    }

    fun downloadEnd(ex: Exception) {
        downloadCompletion
            ?.run {
                uncaughtException = ex
                downloading = false
                complete(Result.failure(ex))
            }
            ?: error("DownloadCompletion wasn't prepared")
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

    fun noEndpointAvailableError(): Nothing {
        noEndpointAvailableError = true
        throw RuntimeException("No WeaponSkinAssetEndpoint were available")
    }

    private suspend fun downloadImage(
        uuid: String,
        type: WeaponSkinImageType,
    ) {
        runCatching {
            val url = resolveEndpoint()
                .buildImageUrl(uuid, type)

            httpClient.get(
                url = url,
                sessionHandler = { assetSessionHandler(type, this) }
            )

            if (!sessionHandlerInvoked.value) {
                error("Unexpected Behavior on AssetHttpClient, session handler wasn't invoked")
            }
        }.onFailure { ex ->
            if (ex is CancellationException) throw ex
            downloadEnd(ex as Exception)
        }
    }

    private suspend fun assetSessionHandler(
        type: WeaponSkinImageType,
        session: AssetHttpSession
    ) = with(session) {
        val contentSizeLimit = Companion.contentSizeLimit(type)
            .bytes().toIntCheckNoOverflow()
        if (
            contentType != "image"
        ) {
            reject()
            return@with
        }
        if (
            contentSubType != "png" &&
            contentSubType != "jpg" &&
            contentSubType != "jpeg"
        ) {
            reject()
            return@with
        }
        val contentLength = contentLength
        val bb = when {
            contentLength != null && contentLength <= contentSizeLimit -> {
                ByteBuffer.allocate(contentLength.toInt())
            }
            // TODO: ask for confirmation
            else -> {
                reject()
                return@with
            }
        }
        consume(bb)
        downloadEnd(
            WeaponSkinRawImage(
                id = id,
                type = type,
                data = bb.apply { flip() }.moveToByteArray()
            )
        )
    }

    companion object {
        fun contentSizeLimit(type: WeaponSkinImageType): ByteUnit {
            return when(type) {
                WeaponSkinImageType.DISPLAY_SMALL -> (512 * 512 * 32).bitByteUnit()
                WeaponSkinImageType.NONE -> 0.byteUnit()
                WeaponSkinImageType.RENDER_FULL -> (512 * 512 * 32).bitByteUnit()
            }
        }
    }
}