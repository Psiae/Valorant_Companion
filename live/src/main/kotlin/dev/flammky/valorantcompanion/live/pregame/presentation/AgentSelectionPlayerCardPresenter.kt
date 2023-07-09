package dev.flammky.valorantcompanion.live.pregame.presentation

import android.os.SystemClock
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.base.resultingLoop
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentRole
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.onSuccess
import dev.flammky.valorantcompanion.pvp.onSuccess
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.coroutines.*
import dev.flammky.valorantcompanion.assets.R as ASSET_R

@Composable
fun rememberAgentSelectionPlayerCardPresenter(
    assetLoaderService: ValorantAssetsService = inject(),
    nameService: ValorantNameService = inject(),
    mmrService: ValorantMMRService = inject()
): AgentSelectionPlayerCardPresenter {
    return remember(assetLoaderService, nameService) {
        AgentSelectionPlayerCardPresenter(assetLoaderService, nameService, mmrService)
    }
}

class AgentSelectionPlayerCardPresenter(
    private val assetLoaderService: ValorantAssetsService,
    private val nameService: ValorantNameService,
    private val mmrService: ValorantMMRService
) {

    // TODO: error broadcaster, this way we can request refresh from the user
    @Composable
    fun present(
        user: String,
        player: PreGamePlayer,
        matchID: String,
    ): AgentSelectionPlayerCardState {
        val name = lookupName(user, player.puuid)
        val rank = lookupRank(user, player.puuid, matchID)
        val agentIdentity = remember(player.characterID) { ValorantAgentIdentity.ofID(player.characterID) }
        val keyToAgentIcon = agentIconWithKey(agentID = player.characterID)
        val keyToRoleIcon = roleIconWithKey(roleID = agentIdentity?.role?.uuid ?: "")
        val keyToTierIcon = rankIconWithKey(rank = rank?.getOrNull())
        return AgentSelectionPlayerCardState(
            playerGameName = name?.getOrNull()?.gameName ?: "",
            playerGameNameTag = name?.getOrNull()?.tagLine ?: "",
            hasSelectedAgent = player.characterSelectionState.isSelectedOrLocked,
            selectedAgentName = agentIdentity?.displayName ?: "",
            selectedAgentIcon = keyToAgentIcon.second,
            selectedAgentIconKey = keyToAgentIcon.first,
            selectedAgentRoleName = agentIdentity?.role?.displayName ?: "",
            selectedAgentRoleIcon = keyToRoleIcon.second,
            selectedAgentRoleIconKey = keyToRoleIcon.first,
            isLockedIn = player.characterSelectionState.isLocked,
            tierName = rank?.getOrNull()?.displayname ?: "",
            tierIcon = keyToTierIcon.second,
            tierIconKey = keyToTierIcon.first,
            isUser = user.isNotBlank() && user == player.puuid
        )
    }

    // TODO: rewrite
    @Composable
    fun present(
        isUser: Boolean,
        player: PreGamePlayer,
        inUserParty: Boolean,
        nameData: Result<PlayerPVPName>?,
        rankData: Result<CompetitiveRank>?,
        index: Int,
    ): AgentSelectionPlayerCardState {
        val nameKey = remember(
            this,
            isUser,
            player.puuid,
            player.identity.incognito,
            nameData,
            inUserParty,
            index
        ) {
            Any()
        }
        val gameName = remember() {
            mutableStateOf("")
        }.apply {
            value = remember(nameKey) {
                nameData?.getOrNull()?.gameName ?: ""
            }
        }
        val tagLine = remember {
            mutableStateOf("")
        }.apply {
            value = remember(nameKey) {
                nameData?.getOrNull()?.tagLine ?: ""
            }
        }

        val selectedAgentIdentity = remember(player.characterID) {
            ValorantAgentIdentity.ofID(player.characterID)
        }

        val agentIcon = remember(selectedAgentIdentity) {
            mutableStateOf<LocalImage<*>?>(
                LocalImage
                    .Resource(
                        agentDisplayIconByName(selectedAgentIdentity?.displayName ?: "")))
        }

        val agentIconRetryKey = remember(selectedAgentIdentity) {
            mutableStateOf(Any())
        }

        val agentRole = selectedAgentIdentity?.role

        val agentRoleIcon = remember() {
            mutableStateOf<LocalImage<*>?>(null)
        }.apply {
            value = remember(agentRole) {
                if (agentRole == null) return@remember null
                LocalImage.Resource(
                    when(agentRole) {
                        ValorantAgentRole.Controller -> ASSET_R.raw.role_controller_displayicon
                        ValorantAgentRole.Duelist -> ASSET_R.raw.role_duelist_displayicon
                        ValorantAgentRole.Initiator -> ASSET_R.raw.role_initiator_displayicon
                        ValorantAgentRole.Sentinel -> ASSET_R.raw.role_sentinel_displayicon
                    }
                )
            }
        }

        val agentRoleIconRetryKey = remember {
            mutableStateOf(Any())
        }.apply {
            value = remember(agentRoleIcon.value) {
                Any()
            }
        }

        val rank = rankData?.getOrNull()

        val tierIcon = remember(rankData) {
            LocalImage.Resource(rankData?.getOrNull()?.let { competitiveRankIcon(it) } ?: 0)
        }

        val tierIconRetryKey = remember(tierIcon) {
            mutableStateOf(Any())
        }

        return AgentSelectionPlayerCardState(
            playerGameName = gameName.value,
            playerGameNameTag = tagLine.value,
            hasSelectedAgent = player.characterSelectionState == CharacterSelectionState.SELECTED ||
                    player.characterSelectionState == CharacterSelectionState.LOCKED,
            selectedAgentName = selectedAgentIdentity?.displayName ?: "",
            selectedAgentIcon = agentIcon.value,
            selectedAgentIconKey = agentIconRetryKey,
            selectedAgentRoleName = selectedAgentIdentity?.role?.displayName ?: "",
            selectedAgentRoleIcon = agentRoleIcon.value,
            selectedAgentRoleIconKey = agentRoleIconRetryKey.value,
            isLockedIn = player.characterSelectionState == CharacterSelectionState.LOCKED,
            tierName = rank?.displayname ?: "",
            // TODO: loader
            tierIcon = tierIcon,
            tierIconKey = tierIconRetryKey,
            isUser = isUser,
            errorMessage = null
        )
    }

    @Composable
    private fun lookupName(
        user: String,
        subject: String
    ): Result<PlayerPVPName>? {
        val returns = remember(user, subject) {
            mutableStateOf<Result<PlayerPVPName>?>(null)
        }

        DisposableEffect(
            user, subject, returns,
            effect = {
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

                scope.launch {
                    var stamp = -1L
                    returns.value = resultingLoop {
                        if (stamp != -1L) {
                            delay(1000 - (stamp - SystemClock.elapsedRealtime()))
                        }
                        stamp = SystemClock.elapsedRealtime()
                        val def = nameService
                            .getPlayerNameAsync(
                                GetPlayerNameRequest(
                                    shard = null,
                                    signedInUserPUUID = user,
                                    lookupPUUIDs = listOf(subject)
                                )
                            )
                        runCatching { def.await() }
                            .onFailure { _ ->
                                def.cancel()
                            }
                            .onSuccess { requestResults ->
                                LOOP_BREAK(requestResults[subject]
                                    ?: error("NameService didn't return requested subject"))
                            }
                    }
                }

                onDispose { scope.cancel() }
            }
        )

        return returns.value
    }

    @Composable
    private fun lookupRank(
        user: String,
        subject: String,
        matchID: String
    ): Result<CompetitiveRank>? {
        val returns = remember(user, subject, matchID) {
            mutableStateOf<Result<CompetitiveRank>?>(null)
        }
        DisposableEffect(
            user, subject, matchID,
            effect = {
                val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                val client = mmrService
                    .createUserClient(user)
                coroutineScope.launch {
                    var stamp = -1L
                    returns.value = resultingLoop {
                        if (stamp != -1L) {
                            delay(1000 - (stamp - SystemClock.elapsedRealtime()))
                        }
                        stamp = SystemClock.elapsedRealtime()
                        val def = client.fetchSeasonalMMRAsync(ValorantSeasons.ACTIVE_STAGED.act.id, subject)
                        runCatching { def.await() }
                            .onFailure { def.cancel() }
                            .onSuccess { pvpResult ->
                                pvpResult.onSuccess { mmrResult ->
                                    mmrResult.onSuccess { mmrData ->
                                        LOOP_BREAK(Result.success(mmrData.competitiveRank))
                                    }
                                }
                            }
                    }
                }

                onDispose { coroutineScope.cancel() ; client.dispose() }
            }
        )

        return returns.value
    }

    @Composable
    private fun agentIconWithKey(
        agentID: String
    ): Pair<Any, LocalImage<*>> {
        val key = remember(agentID) { mutableStateOf(Any()) }
        val image = remember(agentID) { mutableStateOf<LocalImage<*>>(LocalImage.Resource(0)) }
        DisposableEffect(
            key1 = agentID,
            effect = {
                if (agentID.isBlank()) return@DisposableEffect onDispose {  }
                val client = assetLoaderService.createLoaderClient()
                client.loadMemoryCachedAgentIcon(agentID)?.let {
                    key.value = it
                    image.value = it
                }
                onDispose { client.dispose() }
            }
        )
        return remember(key.value) { key.value to image.value }
    }

    @Composable
    private fun roleIconWithKey(
        roleID: String
    ): Pair<Any, LocalImage<*>> {
        val key = remember(roleID) { mutableStateOf(Any()) }
        val image = remember(roleID) { mutableStateOf<LocalImage<*>>(LocalImage.Resource(0)) }
        DisposableEffect(
            key1 = roleID,
            effect = {
                if (roleID.isBlank()) return@DisposableEffect onDispose {  }
                val client = assetLoaderService.createLoaderClient()
                client.loadMemoryCachedRoleIcon(roleID)?.let {
                    key.value = it
                    image.value = it
                }
                onDispose { client.dispose() }
            }
        )
        return remember(key.value) { key.value to image.value }
    }

    @Composable
    private fun rankIconWithKey(
        rank: CompetitiveRank?
    ): Pair<Any, LocalImage<*>> {
        val key = remember(rank) { mutableStateOf(Any()) }
        val image = remember(rank) { mutableStateOf<LocalImage<*>>(LocalImage.Resource(0)) }
        DisposableEffect(
            key1 = rank,
            effect = {
                if (rank == null) return@DisposableEffect onDispose {  }
                val client = assetLoaderService.createLoaderClient()
                client.loadMemoryCachedCompetitiveRankIcon(rank)?.let {
                    key.value = it
                    image.value = it
                }
                onDispose { client.dispose() }
            }
        )
        return remember(key.value) { key.value to image.value }
    }
}

@Deprecated("")
private fun agentDisplayIconFromID(
    id: String
) = agentDisplayIconByName(
    ValorantAgentIdentity.ofID(id)?.displayName ?: ""
)

@Deprecated("")
private fun agentDisplayIconByName(agentName: String): Int {
    // TODO: codename as well
    return when(agentName.lowercase()) {
        "astra" -> ASSET_R.raw.agent_astra_displayicon
        "breach" -> ASSET_R.raw.agent_breach_displayicon
        "brimstone" -> ASSET_R.raw.agent_brimstone_displayicon
        "chamber" -> ASSET_R.raw.agent_chamber_displayicon
        "cypher" -> ASSET_R.raw.agent_cypher_displayicon
        "fade" -> ASSET_R.raw.agent_fade_displayicon
        "gekko" -> ASSET_R.raw.agent_gekko_displayicon
        "harbor" -> ASSET_R.raw.agent_harbor_displayicon
        "jett" -> ASSET_R.raw.agent_jett_displayicon
        "kayo", "kay/o", "grenadier" -> ASSET_R.raw.agent_grenadier_displayicon
        "killjoy" -> ASSET_R.raw.agent_killjoy_displayicon
        "neon" -> ASSET_R.raw.agent_neon_displayicon
        "omen" -> ASSET_R.raw.agent_omen_displayicon
        "phoenix" -> ASSET_R.raw.agent_phoenix_displayicon
        "raze" -> ASSET_R.raw.agent_raze_displayicon
        "reyna" -> ASSET_R.raw.agent_reyna_displayicon
        "sage" -> ASSET_R.raw.agent_sage_displayicon
        "skye" -> ASSET_R.raw.agent_skye_displayicon
        "sova" -> ASSET_R.raw.agent_sova_displayicon
        "viper" -> ASSET_R.raw.agent_viper_displayicon
        "yoru" -> ASSET_R.raw.agent_yoru_displayicon
        else -> 0
    }
}

private fun competitiveRankIcon(
    rank: CompetitiveRank
): Int {
    return when(rank) {
        CompetitiveRank.ASCENDANT_1 -> ASSET_R.raw.rank_ascendant1_smallicon
        CompetitiveRank.ASCENDANT_2 -> ASSET_R.raw.rank_ascendant2_smallicon
        CompetitiveRank.ASCENDANT_3 -> ASSET_R.raw.rank_ascendant3_smallicon
        CompetitiveRank.BRONZE_1 -> ASSET_R.raw.rank_bronze1_smallicon
        CompetitiveRank.BRONZE_2 -> ASSET_R.raw.rank_bronze2_smallicon
        CompetitiveRank.BRONZE_3 -> ASSET_R.raw.rank_bronze3_smallicon
        CompetitiveRank.DIAMOND_1 -> ASSET_R.raw.rank_diamond1_smallicon
        CompetitiveRank.DIAMOND_2 -> ASSET_R.raw.rank_diamond2_smallicon
        CompetitiveRank.DIAMOND_3 -> ASSET_R.raw.rank_diamond3_smallicon
        CompetitiveRank.GOLD_1 -> ASSET_R.raw.rank_gold1_smallicon
        CompetitiveRank.GOLD_2 -> ASSET_R.raw.rank_gold2_smallicon
        CompetitiveRank.GOLD_3 -> ASSET_R.raw.rank_gold3_smallicon
        CompetitiveRank.IMMORTAL_1 -> ASSET_R.raw.rank_immortal1_smallicon
        CompetitiveRank.IMMORTAL_2 -> ASSET_R.raw.rank_immortal2_smallicon
        CompetitiveRank.IMMORTAL_3 -> ASSET_R.raw.rank_immortal3_smallicon
        CompetitiveRank.IMMORTAL_MERGED -> ASSET_R.raw.rank_immortal3_smallicon
        CompetitiveRank.IRON_1 -> ASSET_R.raw.rank_iron1_smallicon
        CompetitiveRank.IRON_2 -> ASSET_R.raw.rank_iron2_smallicon
        CompetitiveRank.IRON_3 -> ASSET_R.raw.rank_iron3_smallicon
        CompetitiveRank.PLATINUM_1 -> ASSET_R.raw.rank_platinum1_smallicon
        CompetitiveRank.PLATINUM_2 -> ASSET_R.raw.rank_platinum2_smallicon
        CompetitiveRank.PLATINUM_3 -> ASSET_R.raw.rank_platinum3_smallicon
        CompetitiveRank.RADIANT -> ASSET_R.raw.rank_radiant_smallicon
        CompetitiveRank.SILVER_1 -> ASSET_R.raw.rank_silver1_smallicon
        CompetitiveRank.SILVER_2 -> ASSET_R.raw.rank_silver2_smallicon
        CompetitiveRank.SILVER_3 -> ASSET_R.raw.rank_silver3_smallicon
        CompetitiveRank.UNRANKED -> ASSET_R.raw.rank_unranked_smallicon
        CompetitiveRank.UNUSED_1 -> ASSET_R.raw.rank_unranked_smallicon
        CompetitiveRank.UNUSED_2 -> ASSET_R.raw.rank_unranked_smallicon
    }
}