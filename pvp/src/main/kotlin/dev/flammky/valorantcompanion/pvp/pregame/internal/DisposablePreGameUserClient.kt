package dev.flammky.valorantcompanion.pvp.pregame.internal

import dev.flammky.valorantcompanion.PVPClientPlatform
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.base.network.NetworkErrorCodes
import dev.flammky.valorantcompanion.pvp.PVPClient
import dev.flammky.valorantcompanion.pvp.TeamID
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes
import dev.flammky.valorantcompanion.pvp.ex.PlayerNotFoundException
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.party.ex.PlayerPartyNotFoundException
import dev.flammky.valorantcompanion.pvp.pregame.*
import dev.flammky.valorantcompanion.pvp.pregame.ex.PreGameMatchNotFoundException
import dev.flammky.valorantcompanion.pvp.match.ex.UnknownTeamIdException
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.store.DEFAULT_UNLOCKED_AGENTS_IDENTITY
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.IOException
import kotlin.reflect.KClass

internal class DisposablePreGameUserClient(
    private val puuid: String,
    private val httpClient: HttpClient,
    private val auth: RiotAuthService,
    private val geo: RiotGeoRepository,
) : PreGameUserClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun fetchCurrentPreGameMatchData(): Deferred<Result<PreGameMatchData>> {
        val def = CompletableDeferred<Result<PreGameMatchData>>()

        val job = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchUserLatestPreGameData()
                    .onFailure { ex ->
                        ex.printStackTrace()
                    }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.printStackTrace()
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion {
            job.cancel()
        }

        return def
    }

    override fun fetchCurrentPreGameMatchId(): Deferred<PreGameFetchRequestResult<String>> {
        val def = CompletableDeferred<PreGameFetchRequestResult<String>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                PreGameFetchRequestResult.buildCatching {
                    success(fetchUserCurrentPreGameMatchID().getOrThrow())
                }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
            def.invokeOnCompletion { ex ->
                ex?.let { cancel() }
            }
        }

        return def
    }

    override fun createMatchClient(matchID: String): PreGameUserMatchClient {
        return DisposablePreGameUserMatchClient(auth, geo, httpClient, puuid, matchID)
    }

    override fun hasPreGameMatchDataAsync(): Deferred<Result<Boolean>> {
        val def = CompletableDeferred<Result<Boolean>>()

        val job = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                runCatching {
                    try {
                        fetchUserCurrentPreGameMatchID().getOrThrow().isNotBlank()
                    } catch (e: Exception) {
                        when (e) {
                            is PreGameMatchNotFoundException -> false
                            else -> throw e
                        }
                    }
                }.onFailure { ex -> ex.printStackTrace() }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.printStackTrace()
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion { job.cancel() }

        return def
    }

    override fun fetchPingMillisAsync(): Deferred<Result<Map<String, Int>>> {
        val def = CompletableDeferred<Result<Map<String, Int>>>()

        val job = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchUserGamePodPingsFromParty()
                    .onFailure { it.printStackTrace() }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.printStackTrace()
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion {
            job.cancel()
        }

        return def
    }

    override fun fetchPlayerMMRData(
        subjectPUUID: String
    ): Deferred<PreGameFetchRequestResult<PreGamePlayerMMRData>> {
        val def = CompletableDeferred<PreGameFetchRequestResult<PreGamePlayerMMRData>>()

        val job = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchPlayerMMRDataFromPublicMMREndpoint(subjectPUUID)
                    .onFailure { exception, errorCode -> exception.printStackTrace() ; exception.cause?.printStackTrace() }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.printStackTrace()
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion {
            job.cancel()
        }

        return def
    }

    override fun fetchUnlockedAgentsAsync(): Deferred<PreGameFetchRequestResult<List<ValorantAgentIdentity>>> {
        val def = CompletableDeferred<PreGameFetchRequestResult<List<ValorantAgentIdentity>>>()

        val task = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchUserUnlockedAgents()
                    .onFailure { exception, errorCode -> exception.printStackTrace() }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.completeExceptionally(ex)  }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion { ex ->
            if (ex is CancellationException) task.cancel()
        }

        return def
    }

    override fun dispose() {
        coroutineScope.cancel()
        httpClient.dispose()
    }

    override fun lockAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameFetchRequestResult<PreGameMatchData>> {
        val def = CompletableDeferred<PreGameFetchRequestResult<PreGameMatchData>>()

        val task = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                postUserLockAgent(matchID, agentID)
                    .onFailure { exception, errorCode ->
                        exception.printStackTrace()
                    }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.completeExceptionally(ex)  }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion { ex ->
            if (ex is CancellationException) task.cancel()
        }

        return def
    }

    override fun selectAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameFetchRequestResult<PreGameMatchData>> {
        val def = CompletableDeferred<PreGameFetchRequestResult<PreGameMatchData>>()

        val task = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                postUserSelectAgent(matchID, agentID)
                    .onFailure { exception, errorCode ->
                        exception.printStackTrace()
                    }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.completeExceptionally(ex)  }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion { ex ->
            if (ex is CancellationException) task.cancel()
        }

        return def
    }

    private suspend fun fetchUserUnlockedAgents(): PreGameFetchRequestResult<List<ValorantAgentIdentity>> {
        return PreGameFetchRequestResult.buildCatching {

            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo.getGeoShardInfo(puuid)
                ?: error("Unable to retrieve GeoShard info")

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "GET",
                        url = "https://pd.${geo.shard.assignedUrlName}.a.pvp.net/store/v1/" +
                                "entitlements/$puuid/${ItemType.Agent.id}",
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }.getOrElse { ex ->
                // TODO: handle http request exception appropriately
                return@buildCatching failure(
                    ex as Exception,
                    NetworkErrorCodes.NETWORK_ERROR
                )
            }

            // TODO: other status code
            when(response.statusCode) {
                200 -> runCatching {
                    success(userUnlockedAgents(parseStoreOwnedAgentsResponse(response.body.getOrThrow())))
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
            }

            unexpectedResponseError("unable to GET user unlocked agents (${response.statusCode})")
        }
    }

    private suspend fun postUserSelectAgent(
        matchID: String,
        agentID: String
    ): PreGameFetchRequestResult<PreGameMatchData> {
        return PreGameFetchRequestResult.buildCatching {
            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo.getGeoShardInfo(puuid)
                ?: error("Unable to retrieve GeoShard info")

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                                ".a.pvp.net/pregame/v1/matches/$matchID/select/$agentID",
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }.getOrElse { ex ->
                return@buildCatching failure(
                    ex as Exception,
                    NetworkErrorCodes.NETWORK_ERROR
                )
            }

            when (response.statusCode) {
                200 -> runCatching {
                    success(mapPreGameMatchDataResponseToModel(matchID, response.body.getOrThrow()))
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
                404 -> runCatching {
                    val errorCode = response.body.getOrThrow()
                        .expectJsonObject("PlayerPreGameData response")
                        .expectJsonProperty("errorCode")
                        .expectJsonPrimitive("errorCode")
                        .expectNotJsonNull("errorCode")
                        .content
                        .also { expectNonBlankJsonString("errorCode", it) }
                    if (errorCode == "PREGAME_MNF") return@runCatching failure(
                        PreGameMatchNotFoundException(),
                        PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE
                    )
                    error("UNHANDLED ERROR CODE")
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
            }

            unexpectedResponseError("Unable to POST agent lock request (${response.statusCode})")
        }
    }

    private suspend fun postUserLockAgent(
        matchID: String,
        agentID: String
    ): PreGameFetchRequestResult<PreGameMatchData> {
        return PreGameFetchRequestResult.buildCatching {
            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo.getGeoShardInfo(puuid)
                ?: error("Unable to retrieve GeoShard info")

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                                ".a.pvp.net/pregame/v1/matches/$matchID/lock/$agentID",
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }.getOrElse { ex ->
                return@buildCatching failure(
                    ex as Exception,
                    NetworkErrorCodes.NETWORK_ERROR
                )
            }

            when (response.statusCode) {
                200 -> runCatching {
                    success(mapPreGameMatchDataResponseToModel(matchID, response.body.getOrThrow()))
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
                404 -> runCatching {
                    val errorCode = response.body.getOrThrow()
                        .expectJsonObject("PlayerPreGameData response")
                        .expectJsonProperty("errorCode")
                        .expectJsonPrimitive("errorCode")
                        .expectNotJsonNull("errorCode")
                        .content
                        .also { expectNonBlankJsonString("errorCode", it) }
                    if (errorCode == "PREGAME_MNF") {
                        return@runCatching failure(
                            PreGameMatchNotFoundException(),
                            PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE
                        )
                    }
                    error("UNHANDLED ERROR CODE ($errorCode)")
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
            }

            unexpectedResponseError("Unable to POST agent lock request (${response.statusCode})")
        }
    }

    private suspend fun fetchUserLatestPreGameData(): Result<PreGameMatchData> {
        return runCatching {
            val currentPreGameID = fetchUserCurrentPreGameMatchID().getOrThrow()

            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo.getGeoShardInfo(puuid)
                ?: error("Unable to retrieve GeoShard info")

            val response = httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                            ".a.pvp.net/pregame/v1/matches/$currentPreGameID",
                    headers = listOf(
                        "X-Riot-Entitlements-JWT" to entitlement_token,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )

            when (response.statusCode) {
                200 -> runCatching {
                    mapPreGameMatchDataResponseToModel(
                        expectedMatchID = currentPreGameID,
                        body = response.body.getOrThrow()
                    )
                }.onSuccess { result ->
                    return@runCatching result
                }.onFailure { ex ->
                    throw ex
                }
                400 -> /* TODO: retry */ Unit
                404 -> if (
                    response.body.getOrThrow()
                        .expectJsonObject("PlayerPreGameData response")
                        .expectJsonProperty("errorCode")
                        .expectJsonPrimitive("errorCode")
                        .expectNotJsonNull("errorCode")
                        .content
                        .also { expectNonBlankJsonString("errorCode", it) }
                        .let { code ->
                            code == "PREGAME_MNF" || code == "RESOURCE_NOT_FOUND"
                        }
                ) {
                    throw PreGameMatchNotFoundException()
                }
            }

            unexpectedResponseError("Unable to retrieve User match data (${response.statusCode})")
        }
    }

    private suspend fun fetchUserCurrentPreGameMatchID(): Result<String> {
       return runCatching {
           val access_token = auth.get_authorization(puuid).getOrElse {
               throw IllegalStateException("Unable to retrieve access token", it)
           }.access_token
           val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
               throw IllegalStateException("Unable to retrieve entitlement token", it)
           }
           val geo = geo.getGeoShardInfo(puuid) ?: error("Unable to retrieve GeoShard info")

           val response = httpClient.jsonRequest(
               JsonHttpRequest(
                   method = "GET",
                   url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                           ".a.pvp.net/pregame/v1/players/$puuid",
                   headers = listOf(
                       "X-Riot-Entitlements-JWT" to entitlement_token,
                       "Authorization" to "Bearer $access_token"
                   ),
                   body = null
               )
           )

           when (response.statusCode) {
               200 -> return@runCatching retrieveMatchIDFromPreGamePlayerInfo(response.body.getOrThrow())
               400 -> /* TODO: retry */ Unit
               404 -> {
                   if (
                       response.body.getOrThrow()
                           .expectJsonObject("PlayerPreGameMatchInfo response")
                           .expectJsonProperty("errorCode")
                           .expectJsonPrimitive("errorCode")
                           .expectNotJsonNull("errorCode")
                           .content
                           .also { expectNonBlankJsonString("errorCode", it) }
                           .equals("RESOURCE_NOT_FOUND")
                   ) {
                        throw PreGameMatchNotFoundException()
                   }
               }
           }

           unexpectedResponseError("Unable to retrieve user match data (${response.statusCode})")
        }
    }

    private suspend fun fetchUserGamePodPingsFromParty(): Result<Map<String, Int>> {
        return runCatching {

            val partyID = fetchUserPartyId().getOrThrow()

            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token

            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo.getGeoShardInfo(puuid) ?: error("Unable to retrieve GeoShard info")

            /*runCatching refreshPing@ {
                val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                        ".a.pvp.net/parties/v1/parties/$partyID/members/$puuid/refreshPings"
                val response = httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = url,
                        headers = listOf(
                            "X-Riot-ClientVersion" to PVPClient.VERSION,
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }*/

            val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}" +
                    ".a.pvp.net/parties/v1/parties/$partyID"
            val response = httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = url,
                    headers = listOf(
                        "X-Riot-Entitlements-JWT" to entitlement_token,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )
            when(response.statusCode) {
                200 -> {
                    val body = response.body.getOrThrow().expectJsonObject("UserPartyData response body")
                    body
                        .expectJsonProperty("ID")
                        .expectJsonPrimitive("ID")
                        .expectNotJsonNull("ID")
                        .content
                        .also { expectNonBlankJsonString("ID", it) }
                        .let { id ->
                            if (id != partyID) unexpectedResponseError("PartyID mismatch")
                        }
                    body
                        .expectJsonProperty("Members")
                        .expectJsonArray("Members")
                        .forEach { element ->
                            element
                                .expectJsonObjectAsJsonArrayElement("Members")
                                .let { member ->
                                    member
                                        .expectJsonProperty("Subject")
                                        .expectJsonPrimitive("Subject")
                                        .expectNotJsonNull("Subject")
                                        .content
                                        .also {
                                            expectNonBlankJsonString("Subject", it)
                                            if (it != puuid) return@forEach
                                        }
                                    return@runCatching member
                                        .expectJsonProperty("Pings")
                                        .expectJsonArray("Pings")
                                        .associate { element ->
                                            val ping = element.expectJsonObjectAsJsonArrayElement("Pings")
                                            val pingMS = ping
                                                .expectJsonProperty("Ping")
                                                .expectJsonPrimitive("Ping")
                                                .expectNotJsonNull("Ping")
                                                .content
                                                .also { expectJsonNumber("Ping", it) }
                                                .toInt()
                                            val podID = ping
                                                .expectJsonProperty("GamePodID")
                                                .expectJsonPrimitive("GamePodID")
                                                .expectNotJsonNull("GamePodID")
                                                .content
                                                .also { expectNonBlankJsonString("GamePodID", it) }
                                            podID to pingMS
                                        }
                                }
                        }
                }
            }

            unexpectedResponseError("unable to retrieve user party data (${response.statusCode})")
        }
    }

    private suspend fun fetchUserPartyId(): Result<String> {
        return runCatching {
            val access_token = auth.get_authorization(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }
            val geo = geo
                .getGeoShardInfo(puuid)
                ?: error("Unable to retrieve GeoShard info")
            val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/players/$puuid"
            val response = httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = url,
                    headers = listOf(
                        "X-Riot-ClientVersion" to PVPClient.VERSION,
                        "X-Riot-Entitlements-JWT" to entitlement_token,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )
            when (response.statusCode) {
                200 -> {
                    return@runCatching response.body
                        .getOrThrow()
                        .expectJsonObject("PlayerPartyInfo response")
                        .expectJsonProperty("CurrentPartyID")
                        .expectJsonPrimitive("CurrentPartyID")
                        .expectNotJsonNull("CurrentPartyID")
                        .content
                        .also { expectNonBlankJsonString("CurrentPartyID", it) }
                }
                400 -> {
                    // TODO: retry
                }
                404 -> {
                    response.body
                        .getOrThrow()
                        .expectJsonObject("PlayerPartyInfo response")
                        .expectJsonProperty("errorCode")
                        .expectJsonPrimitive("errorCode")
                        .expectNotJsonNull("errorCode")
                        .content
                        .also { expectNonBlankJsonString("errorCode", it) }
                        .let { code ->
                            if (code == "RESOURCE_NOT_FOUND") throw PlayerPartyNotFoundException()
                            if (code == "PLAYER_DOES_NOT_EXIST") throw PlayerNotFoundException()
                        }
                }
            }

            unexpectedResponseError("unable to retrieve user party id (${response.statusCode})")
        }
    }

    private suspend fun fetchPlayerMMRDataFromPublicMMREndpoint(
        subject: String
    ): PreGameFetchRequestResult<PreGamePlayerMMRData> {
        val handle = this.puuid
        return PreGameFetchRequestResult.buildCatching {
            val access_token = auth.get_authorization(handle).getOrElse { ex ->
                return failure(
                    IllegalStateException("Unable to retrieve access token", ex),
                    PVPModuleErrorCodes.AUTH_TOKEN_UNAVAILABLE
                )
            }.access_token

            val entitlement_token = auth.get_entitlement_token(handle).getOrElse { ex ->
                return failure(
                    IllegalStateException("Unable to retrieve entitlement token", ex),
                    PVPModuleErrorCodes.AUTH_TOKEN_UNAVAILABLE
                )
            }

            // we can assume that the user is in the same shard as the subject
            val geo = geo.getGeoShardInfo(handle)
                ?: return failure(
                    IllegalStateException("Unable to retrieve GeoShard info"),
                    PVPModuleErrorCodes.GEOLOCATION_UNAVAILABLE
                )

            val url = "https://pd.${geo.shard.assignedUrlName}.a.pvp.net/mmr/v1/players/$subject"

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "GET",
                        url = url,
                        headers = listOf(
                            "X-Riot-ClientPlatform" to PVPClientPlatform.BASE_64,
                            "X-Riot-ClientVersion" to PVPClient.VERSION,
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }.getOrElse { ex ->
                return@buildCatching failure(
                    exception = ex as Exception,
                    errorCode = when(ex) {
                        is IOException ->  NetworkErrorCodes.NETWORK_ERROR
                        else -> PVPModuleErrorCodes.UNHANDLED_REMOTE_EXCEPTION
                    }
                )
            }

            // TODO: Client version mismatch can cause the API to return 404
            when (response.statusCode) {
                200 -> runCatching {
                    parseCurrentSeasonMMRDataFromPublicMMREndpoint(
                        subject,
                        response.body.getOrThrow()
                    )
                }.onSuccess { data ->
                    return@buildCatching success(
                        data
                    )
                }.onFailure { ex ->
                    return@buildCatching failure(
                        ex as Exception,
                        PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE
                    )
                }
            }

            error("Unhandled HTTP response Code (${response.statusCode})")
        }
    }

    private fun retrieveMatchIDFromPreGamePlayerInfo(
        body: JsonElement
    ): String {
        val propName = "MatchID"
        return body
            .expectJsonObject("PreGamePlayerInfo response body")
            .expectJsonProperty(propName)
            .expectJsonPrimitive(propName)
            .expectNotJsonNull(propName)
            .content
            .also { expectNonBlankJsonString(propName, it) }
    }

    private fun mapPreGameMatchDataResponseToModel(
        expectedMatchID: String,
        body: JsonElement
    ): PreGameMatchData {
        val element = body
            .expectJsonObject("PreGameMatchData response body")
        return PreGameMatchData(
            match_id = run {
                val propName = "ID"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectNonBlankJsonString(propName, it) }
                    .takeIf { it == expectedMatchID }
                    ?: error("MatchID mismatch")
            },
            version = run {
                val propName = "Version"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectNonBlankJsonString(propName, it) }
                    .also { expectJsonNumber(propName, it) }
                    .toLong()
            },
            teams = run {
                val propName = "Teams"
                element
                    .expectJsonProperty(propName)
                    .expectJsonArray(propName)
                    .mapTo(
                        destination = persistentListOf<PreGameTeam>().builder(),
                        transform = { team -> parsePreGameTeam(team, propName) }
                    ).build()
            },
            allyTeam = run {
                val propName = "AllyTeam"
                element
                    .expectJsonProperty(propName)
                    .jsonNullable()
                    ?.expectJsonObject(propName)
                    ?.let { team -> parsePreGameTeam(team, propName) }
            },
            enemyTeam = run {
                val propName = "EnemyTeam"
                element
                    .expectJsonProperty(propName)
                    .jsonNullable()
                    ?.expectJsonObject(propName)
                    ?.let { team -> parsePreGameTeam(team, propName) }
            },
            enemyTeamSize = run {
                val propName = "EnemyTeamSize"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonNumber(propName, it) }
                    .toInt()
            },
            enemyTeamLockCount = run {
                val propName = "EnemyTeamLockCount"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonNumber(propName, it) }
                    .toInt()
            },
            state = run {
                val propName = "PregameState"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
                    .let { str ->
                        PreGameState.parse(str)
                            ?: unexpectedJsonValueError(
                                propName,
                                "unknown PreGameState ($str)"
                            )
                    }
            },
            lastUpdated = run {
                val propName = "LastUpdated"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
                    .let { str ->
                        ISO8601.fromISOString(str)
                    }
            },
            mapID = run {
                val propName = "MapID"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
            },
            team1 = run {
                val propName = "Team1"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
            },
            gamePodId = run {
                val propName = "GamePodID"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
            },
            gameModeId = run {
                val propName = "Mode"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
            },
            queueID = run {
                val propName = "QueueID"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
            },
            provisioningFlow = run {
                val propName = "ProvisioningFlowID"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
            },
            phaseTimeRemainingNS = run {
                val propName = "PhaseTimeRemainingNS"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectJsonNumber(propName, it)
                    }
                    .toLong()
            },
            stepTimeRemainingNS = run {
                val propName = "StepTimeRemainingNS"
                element
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectJsonNumber(propName, it)
                    }
                    .toLong()
            }
        )
    }

    private fun parsePreGameTeam(
        element: JsonElement,
        propertyName: String
    ): PreGameTeam {
        val team =
            element.expectJsonObject(propertyName)
        return PreGameTeam(
            teamID = run {
                val propName = "TeamID"
                team
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propName, it)
                    }
                    .let {
                        TeamID.parse(it)
                            ?: unexpectedTeamIdError("unknown team ID ($it)")
                    }

            },
            players = run {
                val propName = "Players"
                val players = team
                    .expectJsonProperty(propName)
                    .expectJsonArray(propName)
                players.mapTo(
                    destination = persistentListOf<PreGamePlayer>().builder(),
                    transform = { element ->
                        parsePreGamePlayer(element, "element of $propName")
                    }
                ).build()
            }
        )
    }

    private fun parsePreGamePlayer(
        element: JsonElement,
        propertyName: String
    ): PreGamePlayer {
        val player = element.expectJsonObject(propertyName)
        return PreGamePlayer(
            puuid = run {
                val propName = "Subject"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(
                            propertyName = propName,
                            content = it
                        )
                    }
            },
            character_id = run {
                val propName = "CharacterID"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
            },
            preGameCharacterSelectionState = run {
                val propName = "CharacterSelectionState"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .let {
                        PreGameCharacterSelectionState.parse(it)
                            ?: unexpectedJsonValueError(
                                propName,
                                "unknown PreGameCharacterSelectionState ($it)"
                            )
                    }

            },
            preGamePlayerState = run {
                val propName = "PregamePlayerState"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also {
                        expectNonBlankJsonString(propertyName = propName, content = it)
                    }
                    .let {
                        PreGamePlayerState.parse(it)
                            ?: unexpectedJsonValueError(
                                propName,
                                "unknown PreGamePlayerState ($it)"
                            )
                    }
            },
            competitiveTier = run {
                val propName = "CompetitiveTier"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonNumber(propName, it) }
                    .toInt()
            },
            identity = run {
                val propName = "PlayerIdentity"
                player
                    .expectJsonProperty(propName)
                    .let { identity -> parsePlayerIdentity(identity, propName) }
            },
            isCaptain = run {
                val propName = "IsCaptain"
                player
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonBoolean(propName, it) }
                    .toBooleanStrict()
            }
        )
    }

    private fun parsePlayerIdentity(
        element: JsonElement,
        propertyName: String
    ): PreGamePlayerIdentity {
        val identity = element.expectJsonObject(propertyName)
        return PreGamePlayerIdentity(
            puuid = run {
                val propName = "Subject"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectNonBlankJsonString(propName, it) }
            },
            playerCardId = run {
                val propName = "PlayerCardID"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectNonBlankJsonString(propName, it) }
            },
            playerTitleId = run {
                val propName = "PlayerTitleID"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectNonBlankJsonString(propName, it) }
            },
            accountLevel = run {
                val propName = "AccountLevel"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonNumber(propName, it) }
                    .toInt()
            },
            preferredBorderId = run {
                val propName = "PreferredLevelBorderID"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
            },
            incognito = run {
                val propName = "Incognito"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonBoolean(propName, it) }
                    .toBooleanStrict()
            },
            hideAccountLevel = run {
                val propName = "HideAccountLevel"
                identity
                    .expectJsonProperty(propName)
                    .expectJsonPrimitive(propName)
                    .expectNotJsonNull(propName)
                    .content
                    .also { expectJsonBoolean(propName, it) }
                    .toBooleanStrict()
            },
        )
    }

    private fun parseCurrentSeasonMMRDataFromPublicMMREndpoint(
        expectedPUUID: String,
        body: JsonElement,
    ): PreGamePlayerMMRData {
        val obj = body.expectJsonObject("MMR response body")

        return PreGamePlayerMMRData(
            subject = run {
                val prop = "Subject"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { id ->
                        expectNonBlankJsonString(prop, id)
                        if (id != expectedPUUID) unexpectedResponseError("PUUID mismatch")
                    }
            },
            version = run {
                val prop = "Version"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { version -> expectJsonNumber(prop, version) }
            },
            competitiveRank = run {
                val prop = "QueueSkills"
                val currentSeason = ValorantSeasons.ACTIVE_STAGED
                obj
                    .expectJsonProperty(prop)
                    .expectJsonObject(prop)
                    .let { skills ->
                        val prop = "competitive"
                        skills
                            .expectJsonProperty(prop)
                            .expectJsonObject(prop)
                    }.let { competitiveSkill ->
                        val prop = "SeasonalInfoBySeasonID"
                        competitiveSkill
                            .expectJsonProperty(prop)
                            .ifJsonNull {
                                return@run CompetitiveRank.UNRANKED
                            }
                            .expectJsonObject(prop)
                    }.let { seasonalInfo ->
                        val prop = currentSeason.act.id
                        seasonalInfo
                            .get(prop)
                            ?.expectJsonObject(prop)
                            ?: return@run CompetitiveRank.UNRANKED
                    }.let { info ->
                        val prop = "CompetitiveTier"
                        info
                            .expectJsonProperty(prop)
                            .expectJsonPrimitive(prop)
                            .expectNotJsonNull(prop)
                            .content
                            .also { tier -> expectJsonNumber(prop, tier) }
                            .toInt()
                            .let { tier ->
                                ValorantCompetitiveRankResolver
                                    .getResolverOfSeason(currentSeason.episode.num, currentSeason.act.num)
                                    .getByTier(tier)
                                    ?: unexpectedJsonValueError(prop, "Unknown Competitive tier ($tier)")
                            }
                    }
            },
            competitiveRankRating = run {
                val prop = "QueueSkills"
                val currentSeason = ValorantSeasons.ACTIVE_STAGED
                obj
                    .expectJsonProperty(prop)
                    .expectJsonObject(prop)
                    .let { skills ->
                        val prop = "competitive"
                        skills
                            .expectJsonProperty(prop)
                            .expectJsonObject(prop)
                    }.let { competitiveSkill ->
                        val prop = "SeasonalInfoBySeasonID"
                        competitiveSkill
                            .expectJsonProperty(prop)
                            .ifJsonNull {
                                return@run 0
                            }
                            .expectJsonObject(prop)
                    }.let { seasonalInfo ->
                        val prop = currentSeason.act.id
                        seasonalInfo
                            .get(prop)
                            ?.expectJsonObject(prop)
                            ?: return@run 0
                    }.let { info ->
                        val prop = "RankedRating"
                        info
                            .expectJsonProperty(prop)
                            .expectJsonPrimitive(prop)
                            .expectNotJsonNull(prop)
                            .content
                            .also { tier -> expectJsonNumber(prop, tier) }
                            .toInt()
                            .also { tier ->
                                if (tier !in 0..100) unexpectedJsonValueError(
                                    prop,
                                    "RankedRating was not in range of 0..100 inclusive"
                                )
                            }
                    }
            }
        )
    }

    private fun parseStoreOwnedAgentsResponse(
        body: JsonElement
    ): List<ValorantAgentIdentity> {
        val obj = body.expectJsonObject("Store Owned Agent response")
        val itemTypeProp = "ItemTypeID"
        obj
            .expectJsonProperty(itemTypeProp)
            .expectJsonPrimitive(itemTypeProp)
            .expectNotJsonNull(itemTypeProp)
            .content
            .also {
                expectNonBlankJsonString(itemTypeProp, it)
                if (it != ItemType.Agent.id) unexpectedJsonValueError(
                    itemTypeProp,
                    "ItemTypeID mismatch, expected Agent"
                )
            }
        val entitlementsProp = "Entitlements"
        return obj
            .expectJsonProperty(entitlementsProp)
            .expectJsonArray(entitlementsProp)
            .mapNotNull {
                val element = it.expectJsonObjectAsJsonArrayElement(entitlementsProp)

                run {
                    val prop = "TypeID"

                    element
                        .expectJsonProperty(prop)
                        .expectJsonPrimitive(prop)
                        .expectNotJsonNull(prop)
                        .content
                        .also { content -> expectNonBlankJsonString(itemTypeProp, content) }
                }

               run {
                   val prop = "ItemID"

                   element
                       .expectJsonProperty(prop)
                       .expectJsonPrimitive(prop)
                       .expectNotJsonNull(prop)
                       .content
                       .also { content ->
                           expectNonBlankJsonString(itemTypeProp, content)
                       }
                       .let { id ->
                           ValorantAgentIdentity.ofID(id)
                       }
               }
            }
    }

    private fun userUnlockedAgents(
        owned: List<ValorantAgentIdentity>
    ): List<ValorantAgentIdentity> {
        return persistentListOf<ValorantAgentIdentity>()
            .builder()
            .apply {
                addAll(DEFAULT_UNLOCKED_AGENTS_IDENTITY)
                addAll(owned)
            }
            .build()
    }

    private fun unexpectedResponseError(msg: String): Nothing {
        throw UnexpectedResponseException(msg)
    }

    private fun unexpectedTeamIdError(msg: String): Nothing {
        throw UnknownTeamIdException(msg)
    }

    private fun unexpectedJsonElementError(
        propertyName: String,
        expectedElement: KClass<out JsonElement>,
        got: JsonElement
    ): Nothing = unexpectedResponseError(
        "expected $propertyName to be ${expectedElement.simpleName} " +
                "but got ${got::class.simpleName} instead"
    )

    private fun unexpectedJsonArrayElementError(
        arrayName: String,
        expectedElement: KClass<out JsonElement>,
        got: JsonElement
    ): Nothing = unexpectedResponseError(
        "expected element of $arrayName to be ${expectedElement.simpleName} " +
                "but got ${got::class.simpleName} instead"
    )

    private fun unexpectedJsonValueError(
        propertyName: String,
        message: String
    ): Nothing = unexpectedResponseError(
        "value of $propertyName was unexpected, message: $message"
    )

    private fun missingJsonPropertyError(
        propertyName: String
    ): Nothing = unexpectedResponseError(
        "$propertyName property not found"
    )

    private fun JsonElement.expectJsonPrimitive(
        propertyName: String
    ): JsonPrimitive {
        return this as? JsonPrimitive
            ?: unexpectedJsonElementError(propertyName, JsonPrimitive::class, this)
    }

    private fun JsonElement.expectJsonObject(
        propertyName: String
    ): JsonObject {
        return this as? JsonObject
            ?: unexpectedJsonElementError(propertyName, JsonObject::class, this)
    }

    private fun JsonElement.expectJsonArray(
        propertyName: String
    ): JsonArray {
        return this as? JsonArray
            ?: unexpectedJsonElementError(propertyName, JsonArray::class, this)
    }

    private fun JsonObject.expectJsonProperty(
        propertyName: String
    ): JsonElement {
        return get(propertyName)
            ?: missingJsonPropertyError(propertyName)
    }

    private fun JsonElement.expectJsonObjectAsJsonArrayElement(
        arrayName: String
    ): JsonObject {
        return this as? JsonObject
            ?: unexpectedJsonArrayElementError(arrayName, JsonObject::class, this)
    }

    private fun JsonPrimitive.expectNotJsonNull(
        propertyName: String
    ): JsonPrimitive {
        if (this is JsonNull) unexpectedJsonValueError(
            propertyName,
            "value is JsonNull"
        )
        return this
    }

    private inline fun JsonElement.ifJsonNull(
        block: () -> Unit
    ): JsonElement {
        if (this is JsonNull) block()
        return this
    }

    private fun JsonElement.jsonNullable(): JsonElement? = if (this is JsonNull) null else this

    private fun expectJsonNumber(
        propertyName: String,
        content: String
    ) {
        if (content.isBlank()) unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value is blank"
        )
        if (content.any { !it.isDigit() }) unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value contains non digit char"
        )
    }

    private fun expectJsonBoolean(
        propertyName: String,
        content: String
    ) {
        when {
            content.isBlank() -> unexpectedJsonValueError(
                propertyName,
                "expected JsonBoolean but value is blank"
            )
            content.any { it.isDigit() } -> unexpectedJsonValueError(
                propertyName,
                "expected JsonNumber but value contains digit char"
            )
            content != "true" && content != "false" -> unexpectedJsonValueError(
                propertyName,
                "expected JsonBoolean but value is not a boolean"
            )
        }
    }

    private fun expectNonBlankJsonString(
        propertyName: String,
        content: String
    ) {
        if (content.isBlank()) unexpectedJsonValueError(
            propertyName,
            "value is blank"
        )
    }
}