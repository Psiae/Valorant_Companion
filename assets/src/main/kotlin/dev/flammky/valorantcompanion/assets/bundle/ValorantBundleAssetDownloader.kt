package dev.flammky.valorantcompanion.assets.bundle

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.base.kt.cast
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.toIntCheckNoOverflow
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ValorantBundleAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantBundleAssetEndpoint,
    private val parser: BundleAssetResponseParser
) {

    private val lifetime = SupervisorJob()
    private val coroutineScope = CoroutineScope(lifetime)

    fun downloadBundleImage(
        id: String,
        acceptableTypes: ImmutableSet<BundleImageType>
    ): ValorantBundleImageDownloadInstance {
        return ValorantBundleImageDownloadInstance(id, acceptableTypes).apply {
            initiateDownloadForInstance(this)
        }
    }

    private fun initiateDownloadForInstance(
        instance: ValorantBundleImageDownloadInstance
    ) {
        if (instance.acceptableTypes.isEmpty()) {
            instance.complete(
                ValorantBundleRawImage(
                    instance.id,
                    BundleImageType.NONE,
                    ByteArray(0)
                )
            )
        }
        coroutineScope.launch(Dispatchers.IO) {

            instance.acceptableTypes.forEach { type ->
                if (type is BundleImageType.NONE) {
                    return@forEach
                }
                runCatching {
                    var result: ByteArray? = null

                    val contentSizeLimit = Companion.contentSizeLimitOfType(type)
                        .bytes()
                        .toIntCheckNoOverflow()

                    assetHttpClient.get(
                        url = endpoint.buildImageUrl(instance.id, type),
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

                    result?.let { barr ->
                        instance.complete(
                            ValorantBundleRawImage(
                                id = instance.id,
                                type = type,
                                data = parser.parseImageResponse(
                                    barr,
                                    Charsets.UTF_8
                                ).getOrElse { return@forEach }
                            )
                        )
                        return@launch
                    }
                }.onFailure { ex -> instance.completeExceptionally(ex.cast()) ; return@launch }
            }
            instance.completeExceptionally(
                // TODO: differentiate between remote error and local error
                IllegalStateException("None of the acceptable types were available")
            )
        }.apply {
            initAsParentCompleter(instance.asDeferred())
        }
    }

    companion object {

        private const val TYPE_DISPLAY_DIMENSION = 1680 * 804
        private const val TYPE_DISPLAY_DEPTH = 32
        private const val TYPE_DISPLAY_SIZE_MAX_BITS = TYPE_DISPLAY_DIMENSION * TYPE_DISPLAY_DEPTH

        private const val TYPE_DISPLAY_VERTICAL_DIMENSION = 320 * 452
        private const val TYPE_DISPLAY_VERTICAL_DEPTH = 32
        private const val TYPE_DISPLAY_VERTICAL_SIZE_MAX_BITS = TYPE_DISPLAY_VERTICAL_DIMENSION * TYPE_DISPLAY_VERTICAL_DEPTH

        fun contentSizeLimitOfType(
            type: BundleImageType
        ): ByteUnit {

            val bits = when(type) {
                BundleImageType.DISPLAY -> TYPE_DISPLAY_SIZE_MAX_BITS.toLong()
                BundleImageType.DISPLAY_VERTICAL -> TYPE_DISPLAY_VERTICAL_SIZE_MAX_BITS.toLong()
                BundleImageType.NONE -> 0
            }

            return ByteUnit.fromBits(bits)
        }
    }
}