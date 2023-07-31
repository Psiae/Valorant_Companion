package dev.flammky.valorantcompanion.assets.spray

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.map.ValorantMapImage
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import dev.flammky.valorantcompanion.base.storage.noIntOverflow
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.*
import java.nio.ByteBuffer

class ValorantSprayAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantSprayAssetEndpoint
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun downloadImage(
        id: String,
        acceptableTypes: ImmutableSet<ValorantSprayImageType>
    ): ValorantSprayDownloadInstance {
        return ValorantSprayDownloadInstance(
            id,
            acceptableTypes
        ).apply { initiateDownloadForInstance(this) }
    }

    private fun initiateDownloadForInstance(
        instance: ValorantSprayDownloadInstance
    ) {
        coroutineScope.launch(Dispatchers.IO) {

            instance.types.forEach { type ->
                runCatching {

                    var result: ByteArray? = null

                    val contentSizeLimit = contentSizeLimitOfImageType(
                        type
                    ).noIntOverflow().bytes().toInt()

                    assetHttpClient.get(
                        url = endpoint.buildImageUrl(instance.uuid, type),
                        sessionHandler = handler@ {
                            if (
                                httpStatusCode !in 200..299
                            ) return@handler reject()
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
                                contentSizeLimit >= contentLength -> ByteBuffer.allocate(contentLength.toInt())
                                else -> return@handler reject()
                            }
                            consume(bb)
                            result = bb.apply { flip() }.moveToByteArray()
                        }
                    )

                    result?.let { arr ->
                        instance.completeWith(
                            Result.success(
                                ValorantSprayImage(
                                    instance.uuid,
                                    type,
                                    arr
                                )
                            )
                        )
                        return@launch
                    }
                }
            }

            // TODO: differentiate between remote error and local error
            instance.completeWith(
                Result.failure(
                    CancellationException("None of the acceptable types were available")
                )
            )

        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { instance.completeExceptionally(ex) }
                check(instance.isCompleted)
            }
            instance.invokeOnCompletion { ex ->
                cancel(ex as? CancellationException?)
            }
        }
    }

    companion object {
        fun contentSizeLimitOfImageType(type: ValorantSprayImageType): ByteUnit {
            return 200.kiloByteUnit()
        }
    }

}