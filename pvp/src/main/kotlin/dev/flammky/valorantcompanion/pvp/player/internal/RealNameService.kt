package dev.flammky.valorantcompanion.pvp.player.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.pvp.BuildConfig
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequestResult
import dev.flammky.valorantcompanion.pvp.player.NameService
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

internal class RealNameService(
    // TODO: should provide builder
    private val httpClient: KtorWrappedHttpClient,
    private val authService: RiotAuthService
) : NameService {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun getPlayerNameAsync(
        request: GetPlayerNameRequest
    ): Deferred<GetPlayerNameRequestResult> {
        // TODO: chunk request

        val def = CompletableDeferred<GetPlayerNameRequestResult>()

        val job = coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val auth_token = authService.get_authorization(request.signedInUserPUUID)
                    .getOrElse { error("Unable to get authorization token") }.access_token
                val entitlement_token = authService.get_entitlement_token(request.signedInUserPUUID)
                    .getOrElse { error("Unable to get entitlement token") }
                val response = httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "PUT",
                        url ="https://pd.${request.shard.assignedUrlName}.a.pvp.net/name-service/v2/players",
                        headers = buildList {
                            add("Authorization" to "Bearer $auth_token")
                            add("X-Riot-Entitlements-JWT" to entitlement_token)
                        },
                        body = buildJsonArray {
                            request.lookupPUUIDs.forEach { add(JsonPrimitive(it)) }
                        }
                    )
                )
                GetPlayerNameRequestResult(
                    run {
                        val mappedResponse = buildMap {
                            response.body.jsonArray
                                .forEachIndexed { index, jsonElement ->
                                    val obj = jsonElement.jsonObject
                                    val subject = obj["Subject"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: error("Subject not found")
                                    if (subject !in request.lookupPUUIDs) {
                                        return@forEachIndexed
                                    }
                                    val displayName = obj["DisplayName"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: error("DisplayName not found")
                                    val gameName = obj["GameName"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: error("GameName not found")
                                    val tagLine = obj["TagLine"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: error("TagLine not found")
                                    put(subject, Result.success(PlayerPVPName(subject, displayName, gameName, tagLine)))
                                }
                        }
                        buildMap {
                            request.lookupPUUIDs.forEach { id ->
                                put(
                                    id,
                                    mappedResponse[id]
                                        ?: Result.failure(UnexpectedResponseException(""))
                                )
                            }
                        }
                    },
                    null
                )
            }.onSuccess {
                def.complete(it)
            }.onFailure { ex ->
                if (BuildConfig.DEBUG) ex.printStackTrace()
                def.complete(GetPlayerNameRequestResult(emptyMap(), ex as Exception))
            }
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let {
                    def.complete(GetPlayerNameRequestResult(emptyMap(), ex as Exception))
                }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion { ex ->
            ex?.let { job.cancel() }
        }

        return def
    }
}