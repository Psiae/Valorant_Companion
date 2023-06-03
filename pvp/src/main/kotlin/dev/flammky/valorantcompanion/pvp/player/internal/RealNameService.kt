package dev.flammky.valorantcompanion.pvp.player.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequestResult
import dev.flammky.valorantcompanion.pvp.player.NameService
import dev.flammky.valorantcompanion.pvp.player.PlayerPvpName
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

        coroutineScope.launch(Dispatchers.IO) {
            def.completeWith(
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
                        buildMap {
                            val array = response.body.jsonArray
                            request.lookupPUUIDs.forEachIndexed { i, e ->
                                put(
                                    e,
                                    runCatching {
                                        val obj = array[i].jsonObject
                                        val puuid = obj["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: error("Subject not found")
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
                                        PlayerPvpName(puuid, displayName, gameName, tagLine)
                                    }
                                )
                            }
                        }
                    )
                }
            )
        }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(ex) } }

        return def
    }
}