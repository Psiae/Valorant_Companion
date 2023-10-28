package dev.flammky.valorantcompanion.assets.valcom.playertitle

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetDownloadInstance
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetDownloader
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*

class ValComPlayerTitleAssetDownloader(
    httpClientFactory: () -> AssetHttpClient
) : PlayerTitleAssetDownloader {

    private val _httpClient by lazy(httpClientFactory)

    override fun downloadIdentity(uuid: String): PlayerTitleAssetDownloadInstance {
        return PlayerTitleIdentityDownloadInstance(uuid, ::_httpClient::get)
    }

    private class PlayerTitleIdentityDownloadInstance(
        uuid: String,
        httpClientFactory: () -> AssetHttpClient
    ): PlayerTitleAssetDownloadInstance(
        uuid
    ) {

        private val _httpClient by lazy(httpClientFactory)

        private val initiated = atomic(false)

        private val _result = CompletableDeferred<Result<ByteArray>>()

        private val coroutineScope = CoroutineScope(SupervisorJob())

        override fun init() {
            if (!initiated.compareAndSet(expect = false, update = true)) {
                return
            }
            doWork()
        }

        override suspend fun awaitResult(): Result<ByteArray> {
            return _result.await()
        }

        override fun asDeferred(): Deferred<Result<ByteArray>> {
            val def = CompletableDeferred<Result<ByteArray>>()
            _result.invokeOnCompletion { ex ->
                if (ex != null)
                    def.cancel(ex.message ?: "", ex.cause)
                else
                    def.complete(_result.getCompleted())
            }
            return def
        }

        private fun doWork() {
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    withContext(SupervisorJob()) {
                        val httpClient = _httpClient
                        httpClient.get(
                            url = if (ValComPlayerTitleAssetEndpoint.LOCALHOST) {
                                val domain = if (ValComPlayerTitleAssetEndpoint.LOCALHOST_VM) {
                                    // AVD
                                    "10.0.2.2"
                                } else {
                                    "localhost"
                                }
                                "http://$domain/valorantcompanionapi/assets/playertitles/$uuid"
                            } else {
                                "https://valorantcompanionapi.com/assets/playertitles/$uuid"
                            },
                            sessionHandler = {
                                response(this)
                            }
                        )
                    }
                }.onFailure { ex ->
                    _result.completeExceptionally(ex)
                }
            }.initAsParentCompleter(_result)
        }

        private suspend fun ensureSessionActive() {
            _result.ensureActive()
        }

        private suspend fun response(
            session: AssetHttpSession
        ) {
            with(session) {
                if (contentType != "application") {
                    session.reject()
                    return@with
                }
                if (contentSubType != "json") {
                    session.reject()
                    return@with
                }
                val contentSizeLimit = Companion.IDENTITY_RESPONSE_SIZE_LIMIT.bytes().toInt()
                val contentLength = contentLength
                    ?.let { contentLength ->
                        if (contentLength > contentSizeLimit) {
                            session.reject()
                            return@with
                        }
                        contentLength
                    }
                val limit = when {
                    contentLength == null -> contentSizeLimit
                    contentSizeLimit >= contentLength -> contentLength.toInt()
                    else -> return@with reject()
                }
                completeWith(consumeToByteArray(limit))
            }
            error("Unexpected Response")
        }

        private fun completeWith(
            byteArray: ByteArray
        ) {
            _result.complete(Result.success(byteArray))
        }
    }

    companion object {

        val IDENTITY_RESPONSE_SIZE_LIMIT = 5.kiloByteUnit()
    }
}