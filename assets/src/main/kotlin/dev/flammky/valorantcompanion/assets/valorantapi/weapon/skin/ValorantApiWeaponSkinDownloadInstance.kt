package dev.flammky.valorantcompanion.assets.valorantapi.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonArray
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinAssetEndpointResolver
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinEndpoint
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinsAssetDownloadInstance
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.megaByteUnit
import kotlinx.atomicfu.atomic
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class ValorantApiWeaponSkinsAssetDownloadInstance(
    private val coroutineScope: CoroutineScope,
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient,
) : WeaponSkinsAssetDownloadInstance() {
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
                val url = endpoint.buildAllSkinsUrl()

                httpClient.get(
                    url = url,
                    sessionHandler = ::assetSessionHandler
                )

                if (!sessionHandlerInvoked.value) {
                    error("Unexpected Behavior on AssetHttpClient, session handler wasn't invoked")
                }

                throw RuntimeException("Response Not Acceptable")
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
        if (httpStatusCode !in 200..299) {
            session.reject()
            return@with
        }
        if (contentType != "application") {
            session.reject()
            return@with
        }
        if (contentSubType != "json") {
            session.reject()
            return@with
        }
        val sizeLimit = SKINS_SIZE_LIMIT.bytes().toInt()
        val contentLength = contentLength
            ?.let { contentLength ->
                if (contentLength > sizeLimit) {
                    session.reject()
                    return@with
                }
                contentLength
            }
        val limit = when {
            contentLength == null -> sizeLimit
            sizeLimit >= contentLength -> contentLength.toInt()
            else -> return@with reject()
        }
        completeWith(consumeToByteArray(limit))
    }

    private fun completeWith(
        byteArray: ByteArray
    ) {
        def.complete(
            runCatching {
                val json = Json.parseToJsonElement(String(byteArray))

                json
                    .expectJsonObject("WeaponSkinsAssetsResponseBody")
                    .expectJsonProperty("data")
                    .expectJsonArray("WeaponSkinsAssetsResponseBody;data")
                    .toString().encodeToByteArray()
            }
        )
    }

    override suspend fun awaitResult(): Result<ByteArray> {
        return def.await()
    }

    override fun asDeferred(): Deferred<Result<ByteArray>> {
        return def
    }

    companion object {
        val SKINS_SIZE_LIMIT = 5.megaByteUnit()
    }
}