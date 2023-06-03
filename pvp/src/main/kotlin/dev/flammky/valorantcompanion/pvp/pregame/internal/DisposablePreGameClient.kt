package dev.flammky.valorantcompanion.pvp.pregame.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.TeamID
import dev.flammky.valorantcompanion.pvp.date.ISO8601
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.pvp.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.pregame.*
import dev.flammky.valorantcompanion.pvp.pregame.ex.PreGameNotFoundException
import dev.flammky.valorantcompanion.pvp.pregame.ex.UnknownTeamIdException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

internal class DisposablePreGameClient(
    private val puuid: String,
    private val httpClient: HttpClient,
    private val auth: RiotAuthService,
    private val geo: RiotGeoRepository
) : PreGameClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun fetchCurrentPreGameMatchData(): Deferred<Result<PreGameMatchData>> {
        val def = CompletableDeferred<Result<PreGameMatchData>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                getUserLatestPreGameData()
                    .onFailure { ex -> ex.printStackTrace() }
            )
        }.invokeOnCompletion { ex ->
            ex?.printStackTrace()
            ex?.let { def.completeExceptionally(ex) }
            check(def.isCompleted)
        }

        return def
    }

    override fun hasPreGameMatchData(): Deferred<Result<Boolean>> {
        val def = CompletableDeferred<Result<Boolean>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                runCatching {
                    try {
                        getUserCurrentPreGameMatchID().getOrThrow().isNotBlank()
                    } catch (e: Exception) {
                        when (e) {
                            is PreGameNotFoundException -> false
                            else -> throw e
                        }
                    }
                }.onFailure { ex -> ex.printStackTrace() }
            )
        }.invokeOnCompletion { ex ->
            ex?.let { def.completeExceptionally(ex) }
            check(def.isCompleted)
        }

        return def
    }

    override fun dispose() {
        coroutineScope.cancel()
    }

    override fun lockAgent(agentID: String): Deferred<Result<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun selectAgent(agentID: String): Deferred<Result<Boolean>> {
        TODO("Not yet implemented")
    }

    private suspend fun getUserLatestPreGameData(): Result<PreGameMatchData> {
        return runCatching {
            val currentPreGameID = getUserCurrentPreGameMatchID().getOrThrow()

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
                200 -> return@runCatching mapPreGameMatchDataResponseToModel(
                    expectedMatchID = currentPreGameID,
                    body = response.body
                )
                400 -> /* TODO: retry */ Unit
                404 -> {
                    if (
                        response.body.jsonObjectOrNull
                            ?.get("errorCode")
                            ?.jsonPrimitiveOrNull
                            ?.content == "PREGAME_MNF"
                    ) {
                        throw PreGameNotFoundException()
                    }
                }
            }

            unexpectedResponseError("Unable to retrieve User match data (${response.statusCode})")
        }
    }

    private suspend fun getUserCurrentPreGameMatchID(): Result<String> {
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
               200 -> return@runCatching retrieveMatchIDFromPreGamePlayerInfo(response.body)
               400 -> /* TODO: retry */ Unit
               404 -> {
                   if (
                       response.body.jsonObjectOrNull
                           ?.get("errorCode")
                           ?.jsonPrimitiveOrNull
                           ?.content == "RESOURCE_NOT_FOUND"
                   ) {
                        throw PreGameNotFoundException()
                   }
               }
           }

           unexpectedResponseError("Unable to retrieve user match data (${response.statusCode})")
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
                        ISO8601.fromStr(str)
                    }
            },
            mapId = run {
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
            queueId = run {
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
                val propName = "PlayerCardID"
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