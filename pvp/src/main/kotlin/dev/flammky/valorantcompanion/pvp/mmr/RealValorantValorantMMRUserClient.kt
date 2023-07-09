package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.PVPClientPlatform
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.network.NetworkErrorCodes
import dev.flammky.valorantcompanion.pvp.PVPAsyncRequestResult
import dev.flammky.valorantcompanion.pvp.PVPClient
import dev.flammky.valorantcompanion.pvp.RateLimitInfo
import dev.flammky.valorantcompanion.pvp.date.ISO8601
import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.http.HTTP_REQUEST_RECEIVED_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.httpDateFormat
import dev.flammky.valorantcompanion.pvp.match.ex.UnknownTeamIdException
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.pvp.tier.ValorantCompetitiveRankResolver
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.IOException
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

internal class RealValorantValorantMMRUserClient(
    val puuid: String,
    private val httpClient: HttpClient,
    private val auth: RiotAuthService,
    private val geo: RiotGeoRepository
) : ValorantMMRUserClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun fetchSeasonalMMRAsync(
        season: String,
        subject: String
    ): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>> {
        val def = CompletableDeferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(fetchSeasonalMMRFromPublicEndpoint(subject))
        }.apply {
            invokeOnCompletion { ex -> if (ex is Throwable) def.completeExceptionally(ex) }
            def.invokeOnCompletion { ex -> if (ex is Throwable) cancel(ex.message ?: "", ex) }
        }

        return def
    }

    override fun createMatchClient(matchID: String): ValorantMMRUserMatchClient {
        TODO("Not yet implemented")
    }

    private suspend fun fetchSeasonalMMRFromPublicEndpoint(
        subject: String
    ): PVPAsyncRequestResult<FetchSeasonalMMRResult> {
        val handle = puuid
        return PVPAsyncRequestResult.buildCatching {
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
                        response.body
                    )
                }.onSuccess { data ->
                    return@buildCatching success(
                        FetchSeasonalMMRResult.success(data)
                    )
                }.onFailure { ex ->
                    return@buildCatching failure(
                        ex as Exception,
                        PVPModuleErrorCodes.UNEXPECTED_REMOTE_RESPONSE
                    )
                }
                429 -> runCatching {
                    RateLimitInfo(
                        remoteServerStamp = run {
                            response.headers["date"]?.let { data ->
                                runCatching {
                                    val date = httpDateFormat().parse(data)
                                    ISO8601.fromEpochMilli(date.time)
                                }.getOrNull()
                            }
                        },
                        deviceClockStamp = run {
                            response.getResponseProperty(
                                HTTP_REQUEST_RECEIVED_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS
                            ) as? Long
                        },
                        retryAfter = run {
                            response.headers["retry-after"]
                                ?.let { value -> value.toLongOrNull()?.seconds }
                        }
                    )
                }.onSuccess { data ->
                    return@buildCatching success(
                        FetchSeasonalMMRResult.failure(data)
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

    private suspend fun fetchSeasonalMMRFromPublicEndpoint(
        subject: String,
        activeMatch: String
    ): PVPAsyncRequestResult<FetchSeasonalMMRResult> {
        // TODO: check cache
        // TODO: cache
        return fetchSeasonalMMRFromPublicEndpoint(subject)
    }

    private fun parseCurrentSeasonMMRDataFromPublicMMREndpoint(
        expectedPUUID: String,
        body: JsonElement,
    ): SeasonalMMRData {
        val obj = body.expectJsonObject("MMR response body")

        return SeasonalMMRData(
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
            season = ValorantSeasons.ACTIVE_STAGED,
            competitiveTier = run {
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
                        val prop = "CompetitiveTier"
                        info
                            .expectJsonProperty(prop)
                            .expectJsonPrimitive(prop)
                            .expectNotJsonNull(prop)
                            .content
                            .also { tier -> expectJsonNumber(prop, tier) }
                            .toInt()
                    }
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
            rankRating = run {
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
                                if (tier < 0) unexpectedJsonValueError(
                                    prop,
                                    "RankedRating was less than 0"
                                )
                            }
                    }
            }
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

    override fun dispose() {
        coroutineScope.cancel()
    }
}