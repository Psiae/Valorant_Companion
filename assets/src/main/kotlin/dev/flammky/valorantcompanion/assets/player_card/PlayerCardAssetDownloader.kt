package dev.flammky.valorantcompanion.assets.player_card

import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.ex.AssetNotFoundException
import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import dev.flammky.valorantcompanion.base.storage.noIntOverflow
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*
import java.nio.ByteBuffer

class PlayerCardAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantPlayerCardAssetEndpoint
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun downloadArt(
        id: String,
        types: ImmutableSet<PlayerCardArtType>
    ): PlayerCardAssetDownloadInstance {
        return PlayerCardAssetDownloadInstance(id, types)
            .apply {
                initiateDownloadForInstance(this)
            }
    }

    private fun initiateDownloadForInstance(instance: PlayerCardAssetDownloadInstance) {
        coroutineScope.launch(Dispatchers.IO) {
            instance.inLifetime {
                instance.acceptableTypes.forEach { type ->
                    runCatching {
                        var result: ByteArray? = null

                        val contentSizeLimit = contentSizeLimitOfImageType(
                            type
                        ).noIntOverflow().bytes().toInt()

                        assetHttpClient.get(
                            url = endpoint.buildArtUrl(instance.id, type),
                            sessionHandler = handler@ {
                                Log.d(
                                    BuildConfig.LIBRARY_PACKAGE_NAME,
                                    "PlayerCardAssetDownloaderKt: " +
                                            "PlayerCardAssetDownloader_initiateDownloadForInstance_sessionHandler(" +
                                            "method=$httpMethod, " +
                                            "status=$httpStatusCode, " +
                                            "contentType=$contentType, " +
                                            "contentSubType=$contentSubType, " +
                                            "contentLength=$contentLength" +
                                            ")"
                                )
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
                            instance.completeWith(
                                Result.success(
                                    PlayerCardArt(
                                        instance.id,
                                        type,
                                        arr
                                    )
                                )
                            )
                        }
                    }
                }
            }
            // TODO: differentiate between remote error and local error
            instance.completeWith(
                Result.failure(
                    AssetNotFoundException("None of the acceptable types were available")
                )
            )
        }.invokeOnCompletion { ex ->
            ex?.let { instance.completeWith(Result.failure(ex)) }
            check(instance.isCompleted)
        }
    }

    companion object {
        fun contentSizeLimitOfImageType(type: PlayerCardArtType): ByteUnit {
            return when(type) {
                PlayerCardArtType.SMALL -> 100.kiloByteUnit()
                PlayerCardArtType.WIDE -> 200.kiloByteUnit()
                PlayerCardArtType.TALL -> 300.kiloByteUnit()
            }
        }
    }
}

class PlayerCardAssetDownloadInstance(
    val id: String,
    val acceptableTypes: ImmutableSet<PlayerCardArtType>
) {

    private val cancellationHandle: Job = Job()
    private val def = CompletableDeferred<PlayerCardArt>()

    fun invokeOnCompletion(
        block: (t: Throwable?) -> Unit
    ) = def.invokeOnCompletion { t -> block(t) }

    fun cancel() {
        cancellationHandle.cancel()
        def.cancel()
    }

    fun completeWith(result: Result<PlayerCardArt>) {
        result.onSuccess {
            def.complete(it)
        }.onFailure { t ->
            def.completeExceptionally(t)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val result: PlayerCardArt?
        get() = if (def.isCompleted && !def.isCancelled) def.getCompleted() else null

    val isCompleted: Boolean
        get() = def.isCompleted

    suspend fun inLifetime(
        block: suspend () -> Unit
        // TODO: find proper solution
    ) = withContext(Job(cancellationHandle)) { block() }
}

class PlayerCardArt(
    val id: String,
    val type: PlayerCardArtType,
    val barr: ByteArray
)