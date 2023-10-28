package dev.flammky.valorantcompanion.assets.valorantapi.playertitle

import android.util.Log
import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonArray
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetDownloadInstance
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleAssetDownloader
import dev.flammky.valorantcompanion.assets.player_title.PlayerTitleIdentity
import dev.flammky.valorantcompanion.base.kt.coroutines.cancelParentOnCancellation
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import io.ktor.http.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject

class ValorantApiTitleAssetDownloader(
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
            def.cancelParentOnCancellation(_result)
            return def
        }

        private fun doWork() {
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    withContext(SupervisorJob()) {
                        val httpClient = _httpClient
                        httpClient.get(
                            url = "https://valorant-api.com/v1/playertitles/$uuid",
                            sessionHandler = {
                                response(this)
                            }
                        )
                    }
                }.onFailure { ex ->
                    Log.d("DEBUG", "onFailure=$ex")
                    _result.completeExceptionally(ex)
                    return@launch
                }
                _result.completeExceptionally(RuntimeException("ValorantApiAssetDownloader did not process response"))
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
                return completeWith(consumeToByteArray(limit))
            }
            error("Unexpected Response")
        }

        private fun completeWith(
            byteArray: ByteArray
        ) {
            Log.d("DEBUG", "completeWith")
            _result.complete(
                runCatching {
                    val json = Json.parseToJsonElement(String(byteArray))

                    val data = json
                        .expectJsonObject("GetPlayerTitleIdentity")
                        .expectJsonProperty("data")
                        .expectJsonObject("GetPlayerTitleIdentity;data")

                    val transform = buildJsonObject {
                        put("uuid", data.expectJsonProperty("uuid"))
                        put("description", data.expectJsonProperty("displayName"))
                        put("titleText", data.expectJsonProperty("titleText"))
                        put("isHiddenIfNotOwned", data.expectJsonProperty("isHiddenIfNotOwned"))
                    }

                    transform.toString().toByteArray()
                }
            )
        }
    }

    companion object {

        val IDENTITY_RESPONSE_SIZE_LIMIT = 5.kiloByteUnit()
    }
}