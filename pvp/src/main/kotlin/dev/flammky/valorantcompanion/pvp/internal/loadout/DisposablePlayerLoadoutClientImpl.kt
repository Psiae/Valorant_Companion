package dev.flammky.valorantcompanion.pvp.internal.loadout

import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.ext.jsonArrayOrNull
import dev.flammky.valorantcompanion.pvp.ext.jsonObjectOrNull
import dev.flammky.valorantcompanion.pvp.ext.jsonPrimitiveOrNull
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.http.JsonHttpRequest
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.loadout.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

// TODO: user persistentList over ArrayList
internal class DisposablePlayerLoadoutClientImpl(
    private val repo: PlayerLoadoutRepositoryImpl,
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val httpClient: HttpClient
) : PlayerLoadoutClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    // TODO: global fetch conflator
    private val fetchConflator = FetchConflator()

    // what was I'm on when I wrote this ???
    override fun getLatestCachedLoadoutAsync(puuid: String): Result<PlayerLoadout?> {
       return runBlocking { repo.getCached(puuid) }
    }

    override fun fetchPlayerLoadoutAsync(puuid: String): Deferred<Result<PlayerLoadout>> {
        return coroutineScope.async(Dispatchers.IO) {
            runCatching {
                fetchConflator.fetch(puuid) {
                    val fetch = fetchPlayerLoadout(puuid).getOrThrow()
                    repo.update(puuid, fetch)
                    fetch
                }
            }
        }
    }

    private suspend fun fetchPlayerLoadout(puuid: String): Result<PlayerLoadout> {
        val entitlement = auth.get_entitlement_token(puuid).getOrElse {
            return Result.failure(it)
        }
        val authTokens = auth.get_authorization_tokens(puuid).getOrElse {
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
                element.jsonArrayOrNull
                    ?.let { array ->
                        array.map { arrayElement ->
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
                        }
                    }
                    ?: run {
                        (element as? JsonNull)
                            ?.let { emptyList() }
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
                    ?.let { array ->
                        array.map { arrayElement ->
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
                                    val sprayId = spray["SprayID"]
                                        ?.let { sprayId ->
                                            sprayId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                                ?: return Result.failure(
                                                    expectedJsonPrimitive(sprayId)
                                                )
                                        }
                                        ?: return Result.failure(
                                            UnexpectedResponseException(
                                                "SprayID not found"
                                            )
                                        )
                                    val sprayLevelId = spray["SprayLevelID"]
                                        ?.let { sprayLevelId ->
                                            sprayLevelId.jsonPrimitiveOrNull?.toString()?.removeSurrounding("\"")
                                                ?: return Result.failure(
                                                    expectedJsonPrimitive(sprayLevelId)
                                                )
                                        }
                                        ?: return Result.failure(
                                            UnexpectedResponseException(
                                                "SprayLevelID not found"
                                            )
                                        )
                                    SprayLoadoutItem(
                                        equipSlotId = equipSlotId,
                                        sprayId = sprayId,
                                        sprayLevelId = sprayLevelId
                                    )
                                }
                                ?: return Result.failure(
                                    expectedJsonObject(arrayElement)
                                )
                        }
                    }
                    ?: run {
                        (element as? JsonNull)
                            ?.let { emptyList() }
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