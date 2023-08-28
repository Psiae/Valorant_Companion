package dev.flammky.valorantcompanion.pvp.loadout.internal

import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.ext.jsonArrayOrNull
import dev.flammky.valorantcompanion.pvp.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.pvp.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonNumber
import dev.flammky.valorantcompanion.pvp.http.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.pvp.http.json.jsonNullable
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.loadout.*
import dev.flammky.valorantcompanion.pvp.store.ItemType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

// TODO: user persistentList over ArrayList
// TODO: Result Wrapper
internal class DisposablePlayerLoadoutClientImpl(
    private val repo: PlayerLoadoutRepositoryImpl,
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val httpClient: HttpClient,
) : PlayerLoadoutClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    // TODO: global fetch conflator
    private val fetchConflator = FetchConflator()

    // what was I'm on when I wrote this ???
    override fun getLatestCachedLoadout(puuid: String): Result<PlayerLoadout?> {
       return runBlocking { repo.getCached(puuid) }
    }

    override fun fetchPlayerLoadoutAsync(puuid: String): Deferred<Result<PlayerLoadout>> {
        return coroutineScope.async(Dispatchers.IO) {
            runCatching {
                fetchConflator.fetch(puuid + "_loadout") {
                    val fetch = fetchPlayerLoadout(puuid).getOrThrow()
                    repo.update(puuid, fetch)
                    fetch
                }
            }
        }
    }

    override fun fetchPlayerAvailableSprayLoadoutAsync(puuid: String): Deferred<Result<PlayerAvailableSprayLoadout>> {
        return coroutineScope.async(Dispatchers.IO) {
            runCatching {
                fetchConflator.fetch(puuid+ "_loadout_avail_spray") {
                    val fetch = fetchPlayerAvailableSprayLoadout(puuid).getOrElse {
                        it.printStackTrace()
                        throw it
                    }
                    repo.updateAvail(puuid, fetch)
                    fetch
                }
            }
        }
    }

    override fun modifyPlayerLoadoutAsync(
        puuid: String,
        data: PlayerLoadoutChangeData
    ): Deferred<Result<PlayerLoadout>> {
        return coroutineScope.async(Dispatchers.IO) {
            putModifyPlayerLoadout(puuid, data)
        }
    }

    private suspend fun fetchPlayerLoadout(puuid: String): Result<PlayerLoadout> {
        val entitlement = auth.get_entitlement_token(puuid).getOrElse {
            return Result.failure(it)
        }
        val authTokens = auth.get_authorization_token(puuid).getOrElse {
            return Result.failure(it)
        }
        val shard = geo.get_shard(puuid).getOrElse {
            return Result.failure(it)
        }
        val response = runCatching {
            httpClient.jsonRequest(
                JsonHttpRequest(
                    method = "GET",
                    url = "https://pd.${shard.assignedUrlName}.a.pvp.net/personalization/v2/players/$puuid/playerloadout",
                    headers = listOf(
                        "X-Riot-Entitlements-JWT" to entitlement,
                        "Authorization" to "Bearer ${authTokens.access_token}"
                    ),
                    body = null
                )
            )
        }.getOrElse {
            return Result.failure(it)
        }
        val obj = response.body.getOrElse {
            return Result.failure(UnexpectedResponseException("Body is not a JSON"))
        }.let { body ->
            body.jsonObjectOrNull ?: return Result.failure(expectedJsonObject(body))
        }

        return parsePlayerLoadoutResponse(puuid, obj)
    }

    private fun parsePlayerLoadoutResponse(
        puuid: String,
        response: JsonObject
    ): Result<PlayerLoadout> {
        val obj = response
        val response_puuid = obj["Subject"]
            ?.let { element ->
                element.jsonPrimitiveOrNull
                    ?.let { elementStr ->
                        elementStr.toString().removeSurrounding("\"").also { str ->
                            if (str != puuid) {
                                return Result.failure(UnexpectedResponseException("API returned different puuid"))
                            }
                        }
                    }
                    ?: return Result.failure(
                        expectedJsonPrimitive(element)
                    )
            }
            ?: return Result.failure(
                UnexpectedResponseException("Subject not found")
            )

        val response_version = obj["Version"]
            ?.let { element ->
                element.jsonPrimitiveOrNull
                    ?.let {
                        it.toString().run {
                            if (!all(Char::isDigit)) {
                                return Result.failure(UnexpectedResponseException("API returned version containing letter"))
                            }
                            toInt()
                        }
                    }
                    ?: return Result.failure(
                        expectedJsonPrimitive(element)
                    )
            }
            ?: return Result.failure(
                UnexpectedResponseException("Version not found")
            )

        val guns = obj["Guns"]
            ?.let { element ->
                element.jsonArrayOrNull?.mapTo(persistentListOf<GunLoadoutItem>().builder()) { arrayElement ->
                    arrayElement.jsonObjectOrNull
                        ?.let { gun ->
                            val id = gun["ID"]
                                ?.let { id ->
                                    id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                        ?: return Result.failure(
                                            expectedJsonPrimitive(id)
                                        )
                                }
                                ?: return Result.failure(
                                    UnexpectedResponseException("ID not found")
                                )
                            val skin_id = gun["SkinID"]
                                ?.let { skin_id ->
                                    skin_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                        ?: return Result.failure(
                                            expectedJsonPrimitive(skin_id)
                                        )
                                }
                                ?: return Result.failure(
                                    UnexpectedResponseException("SkinID not found")
                                )
                            val skin_level_id = gun["SkinLevelID"]
                                ?.let { skin_level_id ->
                                    skin_level_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                        ?: return Result.failure(
                                            expectedJsonPrimitive(skin_level_id)
                                        )
                                }
                                ?: return Result.failure(
                                    UnexpectedResponseException("SkinLevelID not found")
                                )
                            val chroma_id = gun["ChromaID"]
                                ?.let { chroma_id ->
                                    chroma_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                        ?: return Result.failure(
                                            expectedJsonPrimitive(chroma_id)
                                        )
                                }
                                ?: return Result.failure(
                                    UnexpectedResponseException("SkinLevelID not found")
                                )
                            val charm_instance_id = gun["CharmInstanceID"]
                                ?.let { charm_instance_id ->
                                    charm_instance_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                        ?: return Result.failure(
                                            expectedJsonPrimitive(charm_instance_id)
                                        )
                                }
                            val charm_id = charm_instance_id?.let {
                                gun["CharmID"]
                                    ?.let { charm_id ->
                                        charm_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                            ?: return Result.failure(
                                                expectedJsonPrimitive(charm_id)
                                            )
                                    }
                                    ?: return Result.failure(
                                        UnexpectedResponseException("CharmID not found")
                                    )
                            }
                            val charm_level_id = charm_id?.let {
                                gun["CharmLevelID"]
                                    ?.let { charm_level_id ->
                                        charm_level_id.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                            ?: return Result.failure(
                                                expectedJsonPrimitive(charm_level_id)
                                            )
                                    }
                                    ?: return Result.failure(
                                        UnexpectedResponseException("CharmID not found")
                                    )
                            }
                            val attachments = gun["Attachments"]
                                ?.let { attachments ->
                                    attachments.jsonArrayOrNull
                                        ?.map { it.toString() }
                                        ?: return Result.failure(
                                            expectedJsonArray(attachments)
                                        )
                                }
                                ?: return Result.failure(
                                    UnexpectedResponseException("Atachments not found")
                                )
                            GunLoadoutItem(
                                id = id,
                                skinId = skin_id,
                                skinLevelId = skin_level_id,
                                chromaId = chroma_id,
                                charmInstanceId = charm_instance_id,
                                charmId = charm_id,
                                charmLevelId = charm_level_id,
                                attachements = attachments
                            )
                        }
                        ?: return Result.failure(
                            expectedJsonObject(arrayElement)
                        )
                }?.build()
                    ?: run {
                        (element as? JsonNull)
                            ?.let { persistentListOf() }
                            ?: return Result.failure(
                                expectedJsonArrayOrNull(element)
                            )
                    }
            }
            ?: return Result.failure(
                UnexpectedResponseException("Guns not found")
            )
        val sprays = obj["Sprays"]
            ?.let { element ->
                element.jsonArrayOrNull
                    ?.mapTo(persistentListOf<SprayLoadoutItem>().builder()) { arrayElement ->
                        arrayElement.jsonObjectOrNull
                            ?.let { spray ->
                                val equipSlotId = spray["EquipSlotID"]
                                    ?.let { equipSlotId ->
                                        equipSlotId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                            ?: return Result.failure(
                                                expectedJsonPrimitive(equipSlotId)
                                            )
                                    }
                                    ?: return Result.failure(
                                        UnexpectedResponseException(
                                            "EquipSlotID not found"
                                        )
                                    )
                                val sprayId = spray
                                    .expectJsonProperty("SprayID")
                                    .expectJsonPrimitive("SprayID")
                                    .expectNonBlankJsonString("SprayID")
                                    .content
                                val sprayLevelId = spray["SprayLevelID"]
                                    ?.let { sprayLevelId ->
                                        sprayLevelId
                                            .expectJsonPrimitive("SprayLevelID")
                                            .jsonNullable()
                                            ?.expectNonBlankJsonString("SprayLevelID")
                                            ?.content
                                    }
                                SprayLoadoutItem(
                                    equipSlotId = equipSlotId,
                                    sprayId = sprayId,
                                    sprayLevelId = sprayLevelId
                                )
                            }
                            ?: return Result.failure(
                                expectedJsonObject(arrayElement)
                            )
                    }?.build()
                    ?: run {
                        (element as? JsonNull)
                            ?.let { persistentListOf() }
                            ?: return Result.failure(
                                expectedJsonArrayOrNull(element)
                            )
                    }
            }
            ?: return Result.failure(
                UnexpectedResponseException("Sprays not found")
            )
        val identity = obj["Identity"]
            ?.let { element ->
                element.jsonObjectOrNull
                    ?.let { identity ->
                        val playerCardId = identity["PlayerCardID"]
                            ?.let { playerCardId ->
                                playerCardId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                    ?: return Result.failure(
                                        expectedJsonPrimitive(playerCardId)
                                    )
                            }
                            ?: return Result.failure(
                                UnexpectedResponseException("PlayerCardID not found")
                            )
                        val playerTitleId = identity["PlayerTitleID"]
                            ?.let { playerTitleId ->
                                playerTitleId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                    ?: return Result.failure(
                                        expectedJsonPrimitive(playerTitleId)
                                    )
                            }
                            ?: return Result.failure(
                                UnexpectedResponseException("PlayerTitleID not found")
                            )
                        val accountLevel = identity["AccountLevel"]
                            ?.let { accountLevel ->
                                accountLevel.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                    ?.let { str ->
                                        str.toIntOrNull()
                                            ?: return Result.failure(
                                                UnexpectedResponseException("AccountLevel was not an Int")
                                            )
                                    }
                                    ?: return Result.failure(
                                        expectedJsonPrimitive(accountLevel)
                                    )
                            }
                            ?: return Result.failure(
                                UnexpectedResponseException("AccountLevel not found")
                            )
                        val preferredLevelBorderId = identity["PreferredLevelBorderID"]
                            ?.let { preferredLevelBorderId ->
                                preferredLevelBorderId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                    ?: return Result.failure(
                                        expectedJsonPrimitive(preferredLevelBorderId)
                                    )
                            }
                            ?: return Result.failure(
                                UnexpectedResponseException("PreferredLevelBorderID not found")
                            )
                        val hideAccountLevel = identity["HideAccountLevel"]
                            ?.let { hideAccountLevel ->
                                hideAccountLevel.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                    ?.let { str ->
                                        str.toBooleanStrictOrNull()
                                            ?: return Result.failure(
                                                UnexpectedResponseException("HideAccountLevel was not a Boolean")
                                            )
                                    }
                                    ?: return Result.failure(
                                        expectedJsonPrimitive(hideAccountLevel)
                                    )
                            }
                            ?: return Result.failure(
                                UnexpectedResponseException("HideAccountLevel not found")
                            )
                        IdentityLoadout(
                            playerCardId = playerCardId,
                            playerTitleId = playerTitleId,
                            accountLevel = accountLevel,
                            preferredLevelBorderId = preferredLevelBorderId,
                            hideAccountLevel = hideAccountLevel
                        )
                    }
                    ?: return Result.failure(
                        expectedJsonObject(element)
                    )
            }
            ?: return Result.failure(
                UnexpectedResponseException("Identity not found")
            )
        val incognito = obj["Incognito"]
            ?.let { element ->
                element.jsonPrimitiveOrNull?.toString()
                    ?.let { str ->
                        str.toBooleanStrictOrNull()
                            ?: return Result.failure(
                                UnexpectedResponseException("Incognito was not a Boolean")
                            )
                    }
                    ?: return Result.failure(
                        expectedJsonPrimitive(element)
                    )
            }
            ?: return Result.failure(
                UnexpectedResponseException("Incognito not found")
            )
        return Result.success(
            PlayerLoadout(
                response_puuid,
                response_version,
                guns,
                sprays,
                identity,
                incognito
            )
        )
    }

    private suspend fun fetchPlayerAvailableSprayLoadout(
        puuid: String
    ): Result<PlayerAvailableSprayLoadout> {
        return runCatching {
            val access_token = auth.get_authorization_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }

            val shard = geo.get_shard(puuid).getOrElse {
                error("Unable to retrieve GeoShard info")
            }

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "GET",
                        url = "https://pd.${shard.assignedUrlName}.a.pvp.net/store/v1/" +
                                "entitlements/$puuid/${ItemType.Spray.id}",
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = null
                    )
                )
            }.getOrElse { ex ->
                // TODO: handle http request exception appropriately
                throw ex
            }

            // TODO: other status code
            when(response.statusCode) {
                200 -> return@runCatching parseStoreOwnedSpraysResponse(response.body.getOrThrow())
            }

            unexpectedResponseError("unable to GET user unlocked agents (${response.statusCode})")
        }
    }

    private suspend fun putModifyPlayerLoadout(
        puuid: String,
        data: PlayerLoadoutChangeData
    ): Result<PlayerLoadout> {
        return runCatching {
            val access_token = auth.get_authorization_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve access token", it)
            }.access_token
            val entitlement_token = auth.get_entitlement_token(puuid).getOrElse {
                throw IllegalStateException("Unable to retrieve entitlement token", it)
            }

            val shard = geo.get_shard(puuid).getOrElse {
                error("Unable to retrieve GeoShard info")
            }

            val response = runCatching {
                httpClient.jsonRequest(
                    JsonHttpRequest(
                        method = "PUT",
                        url = "https://pd.${shard.assignedUrlName}.a.pvp.net/personalization/v2/players/${puuid}/playerloadout",
                        headers = listOf(
                            "X-Riot-Entitlements-JWT" to entitlement_token,
                            "Authorization" to "Bearer $access_token"
                        ),
                        body = run {
                            buildJsonObject {
                                putJsonArray(
                                    key = "Guns",
                                    builderAction = {
                                        data.guns.forEach { gun ->
                                            buildJsonObject {
                                                put("ID", gun.id)
                                                if (gun.charmInstanceId != null) {
                                                    put("CharmInstanceID", gun.charmInstanceId)
                                                }
                                                if (gun.charmId != null) {
                                                    put("CharmID", gun.charmId)
                                                }
                                                if (gun.charmLevelId != null) {
                                                    put("CharmLevelID", gun.charmLevelId)
                                                }
                                                put("SkinID", gun.skinId)
                                                put("SkinLevelID", gun.skinLevelId)
                                                put("ChromaID", gun.chromaId)
                                                put(
                                                    "Attachments",
                                                    buildJsonArray {
                                                        gun.attachements
                                                    }
                                                )
                                            }.also { add(it) }
                                        }
                                    }
                                )
                                putJsonArray(
                                    key = "Sprays",
                                    builderAction = {
                                        data.sprays.forEach { spray ->
                                            buildJsonObject {
                                                put("EquipSlotID", spray.equipSlotId)
                                                put("SprayID", spray.sprayId)
                                                put("SprayLevelID", spray.sprayLevelId)
                                            }.also { add(it) }
                                        }
                                    }
                                )
                                putJsonObject(
                                    key = "Identity",
                                    builderAction = identityBuilder@ {
                                        put("PlayerCardID", data.identity.playerCardId)
                                        put("PlayerTitleID", data.identity.playerTitleId)
                                        put("AccountLevel", data.identity.accountLevel)
                                        put("PreferredLevelBorderID", data.identity.preferredLevelBorderId)
                                        put("HideAccountLevel", data.identity.hideAccountLevel)
                                    }
                                )
                                put(
                                    key = "Incognito",
                                    value = data.incognito
                                )
                            }
                        }
                    )
                )
            }.getOrElse { ex ->
                // TODO: handle http request exception appropriately
                throw ex
            }

            // TODO: other status code
            when(response.statusCode) {
                200 -> return@runCatching parsePlayerLoadoutResponse(
                    puuid,
                    response.body.getOrThrow().expectJsonObject("PostModifyPlayerLoadout Response")
                ).getOrThrow()
            }

            unexpectedResponseError("unable to POST user loadout modification (${response.statusCode})")
        }
    }

    private fun parseStoreOwnedSpraysResponse(
        body: JsonElement
    ): PlayerAvailableSprayLoadout {
        val obj = body.expectJsonObject("Store Owned Spray response")
        val itemTypeProp = "ItemTypeID"
        obj
            .expectJsonProperty(itemTypeProp)
            .expectJsonPrimitive(itemTypeProp)
            .expectNotJsonNull(itemTypeProp)
            .content
            .also {
                expectNonBlankJsonString(itemTypeProp, it)
                if (it != ItemType.Spray.id) unexpectedJsonValueError(
                    itemTypeProp,
                    "ItemTypeID mismatch, expected Spray"
                )
            }
        val entitlementsProp = "Entitlements"
        return obj
            .expectJsonProperty(entitlementsProp)
            .expectJsonArray(entitlementsProp)
            .mapNotNullTo(persistentListOf<String>().builder()) {
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
                        .also { content -> expectNonBlankJsonString(itemTypeProp, content) }
                }
            }.build().let { sprays -> PlayerAvailableSprayLoadout(sprays) }
    }

    private fun unexpectedResponseError(msg: String): Nothing {
        throw UnexpectedResponseException(msg)
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
        httpClient.dispose()
    }
}

private fun expectedJsonPrimitive(element: JsonElement): Exception {
    return UnexpectedResponseException("expected JsonPrimitive, but got ${element::class.simpleName} instead")
}

private fun expectedJsonObject(element: JsonElement): Exception {
    return UnexpectedResponseException("expected JsonObject, but got ${element::class.simpleName} instead")
}

private fun expectedJsonArray(element: JsonElement): Exception {
    return UnexpectedResponseException("expected JsonArray, but got ${element::class.simpleName} instead")
}

private fun expectedJsonArrayOrNull(element: JsonElement): Exception {
    return UnexpectedResponseException("expected JsonArray or JsonNull, but got ${element::class.simpleName} instead")
}