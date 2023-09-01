package dev.flammky.valorantcompanion.pvp.party.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.PVPClient
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.JsonHttpResponse
import dev.flammky.valorantcompanion.pvp.party.*
import dev.flammky.valorantcompanion.pvp.ex.PlayerNotFoundException
import dev.flammky.valorantcompanion.pvp.http.json.*
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonObject
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonProperty
import dev.flammky.valorantcompanion.pvp.http.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.pvp.party.ex.PlayerPartyNotFoundException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

internal class DisposablePartyServiceClient(
    // TODO: client builder instead
    private val httpClient: HttpClient,
    private val authService: RiotAuthService,
    private val geoRepository: RiotGeoRepository
) : PartyServiceClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val matchmakingPostChannel: SuspendingPostChannel = SuspendingPostChannelImpl(Dispatchers.IO)

    override fun fetchSignedInPlayerPartyDataAsync(puuid: String): Deferred<PlayerPartyData> {
        val def = CompletableDeferred<PlayerPartyData>()

        coroutineScope.launch(Dispatchers.IO) {
            def.completeWith(
                runCatching {
                    val partyId = getPartyId(puuid).getOrThrow()
                    getPlayerPartyData(puuid, partyId).getOrThrow()
                }
            )
        }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(ex) } }

        return def
    }

    // TODO: enqueue
    override fun changePartyMatchmakingQueue(
        request: PartyChangeQueueRequest
    ): Deferred<Result<PartyChangeQueueRequestResult>> {
        return postChangeMatchmakingQueueRequest(request)
    }

    override fun changePartyMatchmakingPreferredPods(
        request: PartyChangePreferredPodsRequest
    ): Deferred<Result<PartyChangePreferredPodsRequestResult>> {
        return postChangeMatchmakingPreferredPods(request)
    }

    override fun partyJoinMatchmaking(puuid: String, partyId: String) {
        return postJoinMatchmakingQueue(puuid, partyId)
    }

    override fun partyLeaveMatchmaking(puuid: String, partyId: String) {
        return postLeaveMatchmakingQueue(puuid, partyId)
    }

    override fun dispose() {
        coroutineScope.cancel()
        httpClient.dispose()
    }

    private suspend fun getPartyId(
        puuid: String
    ): Result<String> {
        return runCatching {
            val geo = geoRepository
                .getGeoShardInfo(puuid)
                ?: error("Geo info not registered")
            val entitlement = authService
                .get_entitlement_token(puuid)
                .getOrThrow()
            val access_token = authService
                .get_authorization(puuid)
                .getOrThrow().access_token
            val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/players/$puuid"
            val response = httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = url,
                    headers = listOf(
                        "X-Riot-ClientVersion" to PVPClient.VERSION,
                        "X-Riot-Entitlements-JWT" to entitlement,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )

            when(response.statusCode) {
                200 -> runCatching {
                    val obj = response.body.getOrThrow().jsonObject
                    val str = obj["CurrentPartyID"]?.jsonPrimitive?.toString()
                    if (str.isNullOrBlank()) {
                        unexpectedResponse("CurrentPartyID not found")
                    }
                    str.removeSurrounding("\"")
                }.onSuccess { data ->
                    return Result.success(data)
                }.onFailure { ex ->
                    return Result.failure(ex)
                }
                404 -> runCatching<Result<String>> {
                    response.body.getOrThrow().jsonObject
                        .get("errorCode")
                        ?.jsonPrimitiveOrNull
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?.let { code ->
                            if (code == "RESOURCE_NOT_FOUND") {
                                return@runCatching Result.failure(PlayerPartyNotFoundException())
                            }
                            if (code == "PLAYER_DOES_NOT_EXIST") {
                                return@runCatching Result.failure(PlayerNotFoundException())
                            }
                        }
                    unexpectedResponse("UNEXPECTED PARTY INFO RESPONSE (404)")
                }.onSuccess { result ->
                    return result
                }.onFailure { ex ->
                    return Result.failure(ex)
                }
            }

            error("Unhandled HTTP response Code (${response.statusCode})")
        }
    }

    private suspend fun getPlayerPartyData(
        puuid: String,
        partyId: String
    ): Result<PlayerPartyData> {
        return runCatching {
            val geo = geoRepository
                .getGeoShardInfo(puuid)
                ?: error("Geo info not registered")
            val entitlement = authService
                .get_entitlement_token(puuid)
                .getOrThrow()
            val access_token = authService
                .get_authorization(puuid)
                .getOrThrow().access_token
            val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/parties/$partyId"
            val response = httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = url,
                    headers = listOf(
                        "X-Riot-Entitlements-JWT" to entitlement,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )
            parsePlayerPartyDataFromResponse(response).getOrThrow()
        }
    }

    private fun parsePlayerPartyDataFromResponse(
        response: JsonHttpResponse
    ): Result<PlayerPartyData> {
        return runCatching {
            PlayerPartyData(
                party_id = run {
                    response.body.getOrThrow().jsonObject["ID"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("ID not found")
                },
                version = run {
                    val str = response.body.getOrThrow().jsonObject["Version"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("Version not found")
                    if (!str.all(Char::isDigit)) {
                        unexpectedResponse("Version contains non Digit Char")
                    }
                    str.toLong()
                },
                client_version = run {
                    response.body.getOrThrow().jsonObject["ClientVersion"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("ClientVersion not found")
                },
                members = run {
                    val arr = response.body.getOrThrow().jsonObject["Members"]?.jsonArray
                        ?: unexpectedResponse("Members not found")
                    arr.map { element ->
                        val member = element.jsonObject
                        PlayerPartyMemberData(
                            uuid = member["Subject"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?.takeIf { it.isNotBlank() }
                                ?: unexpectedResponse("Subject not found"),
                            competitiveTier = run {
                                val str = member["CompetitiveTier"]?.jsonPrimitive
                                    ?.toString()?.removeSurrounding("\"")
                                    ?: unexpectedResponse("CompetitiveTier not found")
                                if (!str.all(Char::isDigit)) {
                                    unexpectedResponse("CompetitiveTier contains non Digit char")
                                }
                                str.toInt()
                            },
                            identity = run {
                                val identity = member["PlayerIdentity"]?.jsonObject
                                    ?: unexpectedResponse("PlayerIdentity not found")
                                PlayerPartyMemberIdentity(
                                    uuid = run {
                                        identity["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?.takeIf { it.isNotBlank() }
                                            ?: unexpectedResponse("Subject not found")
                                    },
                                    cardId = run {
                                        identity["PlayerCardID"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("PlayerCardID not found")
                                    },
                                    titleId = run {
                                        identity["PlayerTitleID"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("PlayerTitleID not found")
                                    },
                                    accountLevel = run {
                                        val str = identity["AccountLevel"]?.jsonPrimitive
                                            ?.toString()
                                            ?: unexpectedResponse("AccountLevel not found")
                                        if (!str.all(Char::isDigit)) {
                                            unexpectedResponse("AccountLevel contains non Digit char")
                                        }
                                        str.toInt()
                                    },
                                    preferred_level_border_id = run {
                                        identity["PreferredLevelBorderID"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("PreferredLevelBorderID not found")
                                    },
                                    incognito = run {
                                        val str = identity["Incognito"]?.jsonPrimitive
                                            ?.toString()
                                            ?: unexpectedResponse("Incognito not found")
                                        str.toBooleanStrictOrNull()
                                            ?: unexpectedResponse("Incognito is not a Boolean")
                                    },
                                    hideAccountLevel = run {
                                        val str = identity["HideAccountLevel"]?.jsonPrimitive
                                            ?.toString()
                                            ?: unexpectedResponse("HideAccountLevel not found")
                                        str.toBooleanStrictOrNull()
                                            ?: unexpectedResponse("HideAccountLevel is not a Boolean")
                                    }
                                )
                            },
                            seasonalBadgeInfo = run {
                                member["SeasonalBadgeInfo"]
                                    ?: unexpectedResponse("SeasonalBadgeInfo not found")
                            },
                            isOwner = run {
                                val prim = member["IsOwner"]?.jsonPrimitive
                                if (prim == null || prim is JsonNull) {
                                    return@run false
                                }
                                prim.toString().toBooleanStrictOrNull()
                                    ?: unexpectedResponse("IsOwner is not a Boolean")
                            },
                            queueEligibleRemainingAccountLevels = run {
                                val str = member["QueueEligibleRemainingAccountLevels"]?.jsonPrimitive?.toString()
                                    ?: unexpectedResponse("QueueEligibleRemainingAccountLevels not found")
                                if (!str.all { it.isDigit() || it == '-' }) {
                                    unexpectedResponse("QueueEligibleRemainingAccountLevels contains non digit char")
                                }
                                str.toInt()
                            },
                            pings = run {
                                val pings = member["Pings"]?.jsonArray
                                    ?: unexpectedResponse("Pings not found")
                                pings.map { element ->
                                    PlayerPartyMemberPing(
                                        pingMs = run {
                                            val str = element.jsonObject["Ping"]?.jsonPrimitive?.toString()
                                                ?: unexpectedResponse("Ping not found")
                                            if (!str.all(Char::isDigit)) {
                                                unexpectedResponse("Ping contains non digit char")
                                            }
                                            str.toInt()
                                        },
                                        gamePodId = run {
                                            element.jsonObject["GamePodID"]?.jsonPrimitive
                                                ?.toString()
                                                ?.removeSurrounding("\"")
                                                ?: unexpectedResponse("GamePodID not found")
                                        }
                                    )
                                }
                            },
                            isReady = run {
                                val str = member["IsReady"]?.jsonPrimitive?.toString()
                                    ?: unexpectedResponse("IsReady not found")
                                str.toBooleanStrictOrNull()
                                    ?: unexpectedResponse("IsReady is not a Boolean")
                            },
                            isModerator = run {
                                val str = member["IsModerator"]?.jsonPrimitive?.toString()
                                    ?: unexpectedResponse("IsModerator not found")
                                str.toBooleanStrictOrNull()
                                    ?: unexpectedResponse("IsModerator is not a Boolean")
                            },
                            useBroadcastHUD = run {
                                val str = member["UseBroadcastHUD"]?.jsonPrimitive?.toString()
                                    ?: unexpectedResponse("UseBroadcastHUD not found")
                                str.toBooleanStrictOrNull()
                                    ?: unexpectedResponse("UseBroadcastHUD is not a Boolean")
                            },
                            platformType = run {
                                member["PlatformType"]?.jsonPrimitive
                                    ?.toString()
                                    ?.removeSurrounding("\"")
                                    ?: unexpectedResponse("PlatformType not found")
                            }
                        )
                    }
                },
                state = run {
                    response.body.getOrThrow().jsonObject["State"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("State not found")
                },
                previousState = run {
                    response.body.getOrThrow().jsonObject["PreviousState"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("PreviousState not found")
                },
                stateTransitionReason = run {
                    response.body.getOrThrow().jsonObject["StateTransitionReason"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("StateTransitionReason not found")
                },
                accessibility = run {
                    val str = response.body.getOrThrow().jsonObject["Accessibility"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("Accessibility not found")
                    PlayerPartyAccessibility.fromStrStrict(str)
                        ?: unexpectedResponse("Accessibility is not defined locally")
                },
                customGameData = run {
                    val data = response.body.getOrThrow().jsonObject["CustomGameData"]?.jsonObject
                        ?: unexpectedResponse("CustomGameData not found")
                    CustomGameData(
                        settings = run {
                            val obj = data["Settings"]?.jsonObject
                                ?: unexpectedResponse("Settings not found")
                            CustomGameDataSettings(
                                map = run {
                                    obj["Map"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: unexpectedResponse("Map not found")
                                },
                                mode = run {
                                    obj["Mode"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: unexpectedResponse("Mode not found")
                                },
                                useBots = run {
                                    val str = obj["UseBots"]?.jsonPrimitive?.toString()
                                        ?: unexpectedResponse("UseBots not found")
                                    str.toBooleanStrictOrNull()
                                        ?: unexpectedResponse("UseBots is not a Boolean")
                                },
                                gamePod = run {
                                    obj["GamePod"]?.jsonPrimitive
                                        ?.toString()
                                        ?.removeSurrounding("\"")
                                        ?: unexpectedResponse("GamePod not found")
                                },
                            )
                        },
                        membership = run {
                            val obj = data["Membership"]?.jsonObject
                                ?: unexpectedResponse("Membership not found")
                            CustomGameDataMembership(
                                teamOne = run {
                                    val arr = obj["teamOne"]?.let {
                                        if (it is JsonNull) return@run null
                                        it.jsonArray
                                    } ?: unexpectedResponse("teamOne not found")
                                    arr.map {
                                        it.jsonObject["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("Subject not found")
                                    }
                                },
                                teamTwo = run {
                                    val arr = obj["teamTwo"]?.let {
                                        if (it is JsonNull) return@run null
                                        it.jsonArray
                                    } ?: unexpectedResponse("teamTwo not found")
                                    arr.map {
                                        it.jsonObject["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("Subject not found")
                                    }
                                },
                                teamSpectate = run {
                                    val arr = obj["teamSpectate"]?.let {
                                        if (it is JsonNull) return@run null
                                        it.jsonArray
                                    } ?: unexpectedResponse("teamSpectate not found")
                                    arr.map {
                                        it.jsonObject["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("Subject not found")
                                    }
                                },
                                teamOneCoaches = run {
                                    val arr = obj["teamOneCoaches"]?.let {
                                        if (it is JsonNull) return@run null
                                        it.jsonArray
                                    } ?: unexpectedResponse("teamOncCoaches not found")
                                    arr.map {
                                        it.jsonObject["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("Subject not found")
                                    }
                                },
                                teamTwoCoaches = run {
                                    val arr = obj["teamTwoCoaches"]?.let {
                                        if (it is JsonNull) return@run null
                                        it.jsonArray
                                    } ?: unexpectedResponse("teamTwoCoaches not found")
                                    arr.map {
                                        it.jsonObject["Subject"]?.jsonPrimitive
                                            ?.toString()
                                            ?.removeSurrounding("\"")
                                            ?: unexpectedResponse("Subject not found")
                                    }
                                },
                            )
                        },
                        maxPartySize = run {
                            val str = data["MaxPartySize"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?: unexpectedResponse("MaxPartySize not found")
                            if (!str.all(Char::isDigit)) {
                                unexpectedResponse("MaxPartySize contains non Digit Char")
                            }
                            str.toInt()
                        },
                        autobalanceEnabled = run {
                            val str = data["AutobalanceEnabled"]?.jsonPrimitive
                                ?.toString()
                                ?: unexpectedResponse("AutobalanceEnabled not found")
                            str.toBooleanStrictOrNull()
                                ?: unexpectedResponse("AutobalanceEnabled is not a Boolean")
                        },
                        autobalanceMinPlayers = run {
                            val str = data["AutobalanceMinPlayers"]?.jsonPrimitive
                                ?.toString()
                                ?: unexpectedResponse("AutobalanceMinPlayers not found")
                            if (!str.all(Char::isDigit)) {
                                unexpectedResponse("AutobalanceMinPlayers contains non Digit Char")
                            }
                            str.toInt()
                        },
                        hasRecoveryData = run {
                            val str = data["HasRecoveryData"]?.jsonPrimitive
                                ?.toString()
                                ?: unexpectedResponse("HasRecoveryData not found")
                            str.toBooleanStrictOrNull()
                                ?: unexpectedResponse("HasRecoveryData is not a Boolean")
                        },
                    )
                },
                matchmakingData = run {
                    val data = response.body.getOrThrow().jsonObject["MatchmakingData"]
                        ?.jsonObject
                        ?: unexpectedResponse("MatchmakingData not found")
                    MatchmakingData(
                        queueId = run {
                            data["QueueID"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?: unexpectedResponse("QueueID not found")
                        },
                        preferredGamePods = run {
                            val arr = data["PreferredGamePods"]?.jsonArray
                                ?: unexpectedResponse("PreferredGamePods not found")
                            arr.map {
                                it.jsonPrimitive
                                    .toString()
                                    .removeSurrounding("\"")
                            }
                        },
                        skillDisparityRRPenalty = run {
                            data["SkillDisparityRRPenalty"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?: unexpectedResponse("SkillDisparityRRPenalty not found")
                        }
                    )
                },
                invites = run {
                    val j = response.body.getOrThrow().jsonObject["Invites"]
                        ?: unexpectedResponse("Invites not found")
                    j as? JsonNull
                },
                requests = run {
                    response.body.getOrThrow().jsonObject["Requests"]?.jsonArray
                        ?: unexpectedResponse("Requests not found")
                },
                queueEntryTime = run {
                    response.body.getOrThrow().jsonObject["QueueEntryTime"]?.jsonPrimitive
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?: unexpectedResponse("QueueEntryTime not found")
                },
                errorNotification = run {
                    val data = response.body.getOrThrow().jsonObject["ErrorNotification"]?.jsonObject
                        ?: unexpectedResponse("ErrorNotification not found")
                    ErrorNotification(
                        errorType = run {
                            data["ErrorType"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?: unexpectedResponse("ErrorType not found")
                        },
                        erroredPlayers = run {
                            val r = data["ErroredPlayers"]
                                ?: unexpectedResponse("ErroredPlayers not found")
                            if (r is JsonNull) return@run null
                            r.jsonArray.map { it.jsonPrimitive.toString() }
                        }
                    )
                },
                restrictedSeconds = run {
                    val str = response.body.getOrThrow().jsonObject["RestrictedSeconds"]?.jsonPrimitive
                        ?.toString()
                        ?: unexpectedResponse("RestrictedSeconds not found")
                    if (str.isBlank()) {
                        unexpectedResponse("RestrictedSeconds is empty")
                    }
                    if (!str.all(Char::isDigit)) {
                        unexpectedResponse("RestrictedSeconds contains non digit number")
                    }
                    str.toInt()
                },
                eligibleQueues = run {
                    val arr = response.body.getOrThrow().jsonObject["EligibleQueues"]?.jsonArray
                        ?: unexpectedResponse("EligibleQueues not found")
                    arr.map { it.jsonPrimitive.toString().removeSurrounding("\"") }
                },
                queueIneligibilities = run {
                    val arr = response.body.getOrThrow().jsonObject["QueueIneligibilities"]?.jsonArray
                        ?: unexpectedResponse("QueueIneligibilities not found")
                    arr.map { element ->
                        val obj = element
                            .expectJsonObject("QueueIneligibilities[]")
                        QueueIneligibilityData(
                            subject = obj
                                .expectJsonProperty("Subject")
                                .expectJsonPrimitive("Subject")
                                .expectNonBlankJsonString("Subject")
                                .content,
                            queueIds = obj
                                .expectJsonProperty("QueueIDs")
                                .expectJsonArray("QueueIDs")
                                .mapTo(persistentListOf<String>().builder()) {
                                    it
                                        .expectJsonPrimitive("QueueIDs[]")
                                        .expectNonBlankJsonString("QueueIDs[]")
                                        .content
                                }
                                .build(),
                            reason = obj
                                .expectJsonProperty("Reason")
                                .expectJsonPrimitive("Reason")
                                .expectNonBlankJsonString("Reason")
                                .content,
                            expiry = obj
                                .expectJsonProperty("Expiry")
                                .expectJsonPrimitive("Expiry")
                                .expectNonBlankJsonString("Expiry")
                                .content
                                .let { runCatching { ISO8601.fromISOString(it) }.getOrNull() }
                        )
                    }
                },
                cheatData = run {
                    val data = response.body.getOrThrow().jsonObject["CheatData"]?.jsonObject
                        ?: unexpectedResponse("CheatData not found")
                    CheatData(
                        gamePodOverride = run {
                            data["GamePodOverride"]?.jsonPrimitive
                                ?.toString()
                                ?.removeSurrounding("\"")
                                ?: unexpectedResponse("GamePodOverride not found")
                        },
                        forcePostGameProcessing = run {
                            val str = data["ForcePostGameProcessing"]?.jsonPrimitive
                                ?.toString()
                                ?: unexpectedResponse("ForcePostGameProcessing not found")
                            str.toBooleanStrictOrNull()
                                ?: unexpectedResponse("ForcePostGameProcessing is not a Boolean")
                        }
                    )
                },
                xpBonuses = run {
                    val arr = response.body.getOrThrow().jsonObject["XPBonuses"]?.jsonArray
                        ?: unexpectedResponse("XPBonuses not found")
                    arr
                },
            )
        }
    }

    private fun parsePlayerPartyDataFromJsonObject(
        obj: JsonObject
    ) {
        // TODO
    }

    private fun postChangeMatchmakingQueueRequest(
        request: PartyChangeQueueRequest
    ): Deferred<Result<PartyChangeQueueRequestResult>> {
        val def = CompletableDeferred<Result<PartyChangeQueueRequestResult>>()
        matchmakingPostChannel.post {
            val task = runCatching<PartyChangeQueueRequestResult> {
                val puuid = request.puuid
                val geo = geoRepository
                    .getGeoShardInfo(puuid)
                    ?: error("Geo info not registered")
                val entitlement_token = authService
                    .get_entitlement_token(puuid)
                    .getOrThrow()
                val access_token = authService
                    .get_authorization(puuid)
                    .getOrThrow().access_token
                val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/parties/${request.partyID}/queue"
                val response = httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = url,
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = buildJsonObject {
                            put("queueId", request.queueID)
                        }
                    )
                )
                parseChangeMatchmakingQueueResponse(response)
            }
            def.complete(task)
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
            check(def.isCompleted)
        }
        return def
    }

    private fun postChangeMatchmakingPreferredPods(
        request: PartyChangePreferredPodsRequest
    ): Deferred<Result<PartyChangePreferredPodsRequestResult>> {
        val def = CompletableDeferred<Result<PartyChangePreferredPodsRequestResult>>()

        matchmakingPostChannel.post {
            val task = runCatching<PartyChangePreferredPodsRequestResult> {
                val puuid = request.puuid
                val geo = geoRepository
                    .getGeoShardInfo(puuid)
                    ?: error("Geo info not registered")
                val entitlement_token = authService
                    .get_entitlement_token(puuid)
                    .getOrThrow()
                val access_token = authService
                    .get_authorization(puuid)
                    .getOrThrow().access_token
                val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/parties/${request.partyID}/preferredgamepods"
                val response = httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = url,
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = buildJsonObject {
                            put(
                                "GamePodIDs",
                                buildJsonArray {
                                    request.preferredPods.forEach { pod -> add(JsonPrimitive(pod)) }
                                }
                            )
                        }
                    )
                )
                parseChangeMatchmakingPreferredPodsResponse(response)
            }
            def.complete(task)
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
            check(def.isCompleted)
        }

        return def
    }

    private fun postJoinMatchmakingQueue(
        puuid: String,
        partyID: String,
    ) {
        matchmakingPostChannel.post {
            runCatching {
                val geo = geoRepository
                    .getGeoShardInfo(puuid)
                    ?: error("Geo info not registered")
                val entitlement_token = authService
                    .get_entitlement_token(puuid)
                    .getOrThrow()
                val access_token = authService
                    .get_authorization(puuid)
                    .getOrThrow().access_token
                val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/parties/$partyID/matchmaking/join"
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = url,
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }
        }.invokeOnCompletion { }
    }

    private fun postLeaveMatchmakingQueue(
        puuid: String,
        partyID: String,
    ) {
        matchmakingPostChannel.post {
            runCatching {
                val geo = geoRepository
                    .getGeoShardInfo(puuid)
                    ?: error("Geo info not registered")
                val entitlement_token = authService
                    .get_entitlement_token(puuid)
                    .getOrThrow()
                val access_token = authService
                    .get_authorization(puuid)
                    .getOrThrow().access_token
                val url = "https://glz-${geo.region.assignedUrlName}-1.${geo.shard.assignedUrlName}.a.pvp.net/parties/v1/parties/$partyID/matchmaking/leave"
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "POST",
                        url = url,
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }
        }.invokeOnCompletion { }
    }

    private fun parseChangeMatchmakingQueueResponse(response: JsonHttpResponse): PartyChangeQueueRequestResult {
        return PartyChangeQueueRequestResult(
            newData = parsePlayerPartyDataFromResponse(response).getOrThrow()
        )
    }

    private fun parseChangeMatchmakingPreferredPodsResponse(response: JsonHttpResponse): PartyChangePreferredPodsRequestResult {
        return PartyChangePreferredPodsRequestResult(
            newData = parsePlayerPartyDataFromResponse(response).getOrThrow()
        )
    }

    private fun unexpectedResponse(msg: String): Nothing {
        throw UnexpectedResponseException(msg)
    }
}