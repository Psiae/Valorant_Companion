package dev.flammky.valorantcompanion.pvp.player.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.BuildConfig
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequestResult
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

internal class RealValorantNameService(
    // TODO: should provide builder
    private val httpClientFactory: () -> HttpClient,
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository
) : ValorantNameService {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val httpClient by lazy { httpClientFactory() }

    override fun getPlayerNameAsync(
        request: GetPlayerNameRequest
    ): Deferred<GetPlayerNameRequestResult> {
        // TODO: chunk request

        val def = CompletableDeferred<GetPlayerNameRequestResult>()

        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val auth_token = authService.get_authorization(request.signedInUserPUUID)
                    .getOrElse { error("Unable to get authorization token") }.access_token
                val entitlement_token = authService.get_entitlement_token(request.signedInUserPUUID)
                    .getOrElse { error("Unable to get entitlement token") }
                val shard = request.shard
                    ?: geoRepository.getGeoShardInfo(request.signedInUserPUUID)
                        ?.shard
                    ?: error("Unable to get GeoInfo")
                val response = httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "PUT",
                        url ="https://pd.${shard.assignedUrlName}.a.pvp.net/name-service/v2/players",
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
                        val providedSubjects = mutableListOf<String>()
                        val mappedResponse = buildMap {
                            response.body.getOrThrow().jsonArray
                                .forEachIndexed { index, jsonElement ->
                                    runCatching {
                                        val obj = jsonElement.jsonObject
                                        val subject = obj["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: error("Subject not found")
                                        providedSubjects.add(subject)
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
                                        put(
                                            subject,
                                            Result.success(
                                                PlayerPVPName(subject, displayName, gameName, tagLine)
                                            )
                                        )
                                    }
                                }
                        }
                        persistentMapOf<String, Result<PlayerPVPName>>()
                            .builder()
                            .apply {
                                request.lookupPUUIDs.forEach { id ->
                                    put(
                                        id,
                                        mappedResponse[id]
                                            ?: Result.failure(
                                                UnexpectedResponseException(
                                                    if (id in providedSubjects) "Endpoint did not return requested subject"
                                                    else "Endpoint returned unexpected data for the given subject"
                                                )
                                            )
                                    )
                                }
                            }
                            .build()
                    },
                    null
                )
            }.onSuccess {
                def.complete(it)
            }.onFailure { ex ->
                if (BuildConfig.DEBUG) ex.printStackTrace()
                def.complete(GetPlayerNameRequestResult(persistentMapOf(), ex as Exception))
            }
        }.apply {
            def.invokeOnCompletion { ex ->
                ex?.let { cancel() }
            }
            invokeOnCompletion { ex ->
                ex?.let {
                    def.complete(GetPlayerNameRequestResult(persistentMapOf(), ex as Exception))
                }
                check(def.isCompleted)
            }
        }

        return def
    }
}