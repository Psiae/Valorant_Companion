package dev.flammky.valorantcompanion.assets.map

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ValorantMapAssetDownloader(
    private val assetHttpClient: AssetHttpClient,
    private val endpoint: ValorantMapAssetEndpoint
) {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun downloadImage(
        id: String,
        types: ImmutableSet<ValorantMapImageType>
    ): ValorantMapAssetDownloadInstance {
        return ValorantMapAssetDownloadInstance(
            id,
            types
        ).apply { initiateDownloadForInstance(this) }
    }

    private fun initiateDownloadForInstance(
        instance: ValorantMapAssetDownloadInstance
    ) {
        coroutineScope.launch {

            instance.types.forEach { type ->
                runCatching {
                    val response = assetHttpClient.get(endpoint.buildImageUrl(instance.id, type))
                    val bb = ByteBuffer.allocate(4_000_000)
                    response.contentChannel.consume(bb)
                    instance.completeWith(
                        Result.success(
                            ValorantMapImage(
                                instance.id,
                                type,
                                bb.apply { flip() }.moveToByteArray()
                            )
                        )
                    )
                    return@launch
                }
            }

            // TODO: differentiate between remote error and local error
            instance.completeWith(
                Result.failure(
                    CancellationException("None of the acceptable types were available")
                )
            )

        }.invokeOnCompletion { ex ->
            ex?.let { instance.completeWith(Result.failure(ex)) }
        }
    }
}