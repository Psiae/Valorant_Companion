package dev.flammky.valorantcompanion.pvp.ingame.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.pvp.TeamID
import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.ingame.*
import dev.flammky.valorantcompanion.pvp.ingame.ex.InGameMatchNotFoundException
import dev.flammky.valorantcompanion.pvp.match.ex.UnknownTeamIdException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

internal class RealInGameUserMatchClient(
    private val puuid: String,
    override val matchID: String,
    private val httpClient: HttpClient,
    private val auth: RiotAuthService,
    private val geo: RiotGeoRepository,
    private val disposeHttpClient: Boolean,
) : InGameUserMatchClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun fetchMatchInfoAsync(): Deferred<InGameFetchRequestResult<InGameMatchInfo>> {
        val def = CompletableDeferred<InGameFetchRequestResult<InGameMatchInfo>>()

        val task = coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchMatchInfo()
                    .onFailure { exception, errorCode ->
                        exception.printStackTrace()
                    }
            )
        }.apply {
            invokeOnCompletion { ex ->
                ex?.let { def.completeExceptionally(ex) }
                check(def.isCompleted)
            }
        }

        def.invokeOnCompletion {
            task.cancel()
        }

        return def
    }

    override fun dispose() {
        coroutineScope.cancel()
        if (disposeHttpClient) httpClient.dispose()
    }

    private suspend fun fetchMatchInfo(): InGameFetchRequestResult<InGameMatchInfo> {
        return InGameFetchRequestResult.buildCatching {
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
                            ".a.pvp.net/core-game/v1/matches/$matchID",
                    headers = listOf(
                        "X-Riot-Entitlements-JWT" to entitlement_token,
                        "Authorization" to "Bearer $access_token"
                    ),
                    body = null
                )
            )

            when (response.statusCode) {
                200 -> runCatching {
                    parseInGameMatchDataFromResponse(response.body.getOrThrow())
                }.onSuccess { data ->
                    return@buildCatching success(data)
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE)
                }
                404 -> runCatching {
                    val propName = "errorCode"
                    val errorCode = response.body.getOrThrow()
                        .expectJsonObject("InGameMatchInfo response")
                        .expectJsonProperty(propName)
                        .expectJsonPrimitive(propName)
                        .expectNotJsonNull(propName)
                        .content
                        .also { expectNonBlankJsonString("errorCode", it) }
                    if (errorCode == "RESOURCE_NOT_FOUND") return@runCatching failure(
                        InGameMatchNotFoundException(),
                        19404
                    )
                    error("UNHANDLED ERROR CODE")
                }.onSuccess { result ->
                    return@buildCatching result
                }.onFailure { ex ->
                    return@buildCatching failure(ex as Exception, PVPModuleErrorCodes.UNHANDLED_EXCEPTION)
                }
            }

            unexpectedResponseError("Unable to GET InGame match info (${response.statusCode})")
        }
    }

    private fun parseInGameMatchDataFromResponse(
        body: JsonElement
    ): InGameMatchInfo {
        val obj = body.expectJsonObject("GET InGameMatchData response")

        return InGameMatchInfo(
            matchID = run {
                val prop = "MatchID"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
                    .takeIf { it == matchID }
                    ?: unexpectedResponseError("MatchID mismatch")
            },
            version = run {
                val prop = "Version"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
                    .also { expectJsonNumber(prop, it) }
            },
            mapID = run {
                val prop = "MapID"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            provisioningFlow = run {
                val prop = "ProvisioningFlow"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            gamePodID = run {
                val prop = "GamePodID"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            gameModeID = run {
              val prop = "ModeID"
              obj
                  .expectJsonProperty(prop)
                  .expectJsonPrimitive(prop)
                  .expectNotJsonNull(prop)
                  .content
                  .also { expectNonBlankJsonString(prop, it) }
            },
            queueID = run {
                val prop = "QueueID"
                val matchmakingDataProp = "MatchmakingData"
                obj
                    .expectJsonProperty(matchmakingDataProp)
                    .ifJsonNull { return@run null }
                    .expectJsonObject(matchmakingDataProp)
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            players = run {
                val prop = "Players"
                val players = obj
                    .expectJsonProperty(prop)
                    .expectJsonArray(prop)
                players.mapTo(
                    destination = persistentListOf<InGamePlayer>().builder(),
                    transform = { element ->
                        parseInGamePlayer(
                            "element of $prop",
                            element,
                        )
                    }
                ).build()
            },
            isRanked = run {
                val prop = "IsRanked"
                val matchmakingDataProp = "MatchmakingData"
                obj
                    .expectJsonProperty(matchmakingDataProp)
                    .ifJsonNull { return@run false }
                    .expectJsonObject(matchmakingDataProp)
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
                    .also { expectJsonBoolean(prop, it) }
                    .toBooleanStrict()
            },
            matchOver = run {
                val prop = "State"
                obj
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
                    .let { state ->
                        state.equals("post_game", ignoreCase = true) ||
                        state.equals("closed", ignoreCase = true)
                    }
            }
        )
    }

    private fun parseInGamePlayer(
        propertyName: String,
        element: JsonElement
    ): InGamePlayer {
        val player = element.expectJsonObject(propertyName)
        return InGamePlayer(
            puuid = run {
                val prop = "Subject"
                player
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            teamID = run {
                val prop = "TeamID"
                player
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
                    .let { id ->
                        TeamID.parse(id)
                            ?: unexpectedTeamIdError("unknown TeamID ($id)")
                    }
            },
            character_id = run {
                val prop = "CharacterID"
                player
                    .expectJsonProperty(prop)
                    .expectJsonPrimitive(prop)
                    .expectNotJsonNull(prop)
                    .content
                    .also { expectNonBlankJsonString(prop, it) }
            },
            playerIdentity = parseInGamePlayerIdentity(
                propertyName,
                player.expectJsonProperty("PlayerIdentity")
            )
        )
    }

    private fun parseInGamePlayerIdentity(
        propertyName: String,
        element: JsonElement
    ): InGamePlayerIdentity {
        val identity = element.expectJsonObject(propertyName)

        return InGamePlayerIdentity(
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

    private fun expectNonBlankJsonString(
        propertyName: String,
        content: String
    ) {
        if (content.isBlank()) unexpectedJsonValueError(
            propertyName,
            "value is blank"
        )
    }

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
}