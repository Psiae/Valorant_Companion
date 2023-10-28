package dev.flammky.valorantcompanion.assets.spray

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.checkNoIntOverflow
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import dev.flammky.valorantcompanion.base.storage.toIntCheckNoOverflow
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.charset.Charset

class ValorantSprayAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantSprayAssetEndpoint,
    private val parser: ValorantSprayAssetResponseParser
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun downloadSprayImage(
        id: String,
        acceptableTypes: ImmutableSet<ValorantSprayImageType>
    ): ValorantSprayImageDownloadInstance {
        return ValorantSprayImageDownloadInstance(
            id,
            acceptableTypes
        ).apply { initiateDownloadForInstance(this) }
    }

    fun downloadSprayLevelImage(
        id: String
    ): ValorantSprayImageDownloadInstance {
        return ValorantSprayImageDownloadInstance(
            id,
            persistentSetOf(ValorantSprayImageType.DISPLAY_ICON)
        )
    }

    fun downloadIdentity(
        id: String
    ): ValorantSprayIdentityDownloadInstance {
        return ValorantSprayIdentityDownloadInstance(
            id
        ).apply { initiateDownloadForInstance(this) }
    }

    private fun initiateDownloadForInstance(
        instance: ValorantSprayImageDownloadInstance
    ) {
        coroutineScope.launch(Dispatchers.IO) {

            instance.types.forEach { type ->
                runCatching {

                    var result: ByteArray? = null

                    val contentSizeLimit = contentSizeLimitOfImageType(
                        type
                    ).bytes().checkNoIntOverflow().toInt()

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

                            val limit = when {
                                contentLength == null -> contentSizeLimit
                                contentSizeLimit >= contentLength -> contentLength.toInt()
                                else -> return@handler reject()
                            }

                            result = consumeToByteArray(limit)
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
                    IllegalStateException("None of the acceptable types were available")
                )
            )

        }.apply {
            initAsParentCompleter(
                instance::invokeOnCompletion,
                instance::cancel,
                instance::isCompleted,
                instance::toString
            )
        }
    }

    private fun initiateDownloadForInstance(
        instance: ValorantSprayIdentityDownloadInstance
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {

                var result: ByteArray? = null

                val contentSizeLimit = identityContentSizeLimit
                    .bytes()
                    .toIntCheckNoOverflow()

                assetHttpClient.get(
                    url = endpoint.buildIdentityUrl(instance.uuid),
                    sessionHandler = handler@ {
                        if (
                            httpStatusCode !in 200..299
                        ) return@handler reject()
                        if (
                            contentType != "application"
                        ) return@handler reject()
                        if (
                            contentSubType != "json"
                        ) return@handler reject()

                        val contentLength = contentLength

                        val limit = when {
                            contentLength == null -> contentSizeLimit
                            contentSizeLimit >= contentLength -> contentLength.toInt()
                            else -> return@handler reject()
                        }

                        result = consumeToByteArray(limit)
                    }
                )

                result?.let { arr ->
                    return@runCatching parser
                        .parseIdentity(instance.uuid, arr, Charsets.UTF_8)
                        .getOrThrow()
                }

                // TODO: differentiate between remote error and local error
                error("None of the acceptable types were available")
            }.fold(
                onSuccess = {
                    instance.completeWith(Result.success(it))
                },
                onFailure = {
                    it.printStackTrace()
                    instance.completeExceptionally(it)
                }
            )
        }.apply {
            initAsParentCompleter(
                instance::invokeOnCompletion,
                instance::cancel,
                instance::isCompleted,
                instance::toString
            )
        }
    }

    companion object {

        val identityContentSizeLimit: ByteUnit = 20.kiloByteUnit()

        fun contentSizeLimitOfImageType(type: ValorantSprayImageType): ByteUnit {
            return 200.kiloByteUnit()
        }
    }

}