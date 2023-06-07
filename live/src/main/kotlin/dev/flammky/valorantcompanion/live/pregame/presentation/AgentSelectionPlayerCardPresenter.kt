package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentRole
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import dev.flammky.valorantcompanion.assets.R as ASSET_R

@Composable
fun rememberAgentSelectionPlayerCardPresenter(
    assetLoaderService: ValorantAssetsService = getFromKoin()
): AgentSelectionPlayerCardPresenter {
    return remember() {
        AgentSelectionPlayerCardPresenter(assetLoaderService)
    }
}

class AgentSelectionPlayerCardPresenter(
    val assetLoaderService: ValorantAssetsService
) {

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
            mutableStateOf<LocalImageData<*>?>(
                LocalImageData
                    .Resource(
                        agentDisplayIconByName(selectedAgentIdentity?.displayName ?: "")))
        }

        val agentIconRetryKey = remember(selectedAgentIdentity) {
            mutableStateOf(Any())
        }

        val agentRole = selectedAgentIdentity?.role

        val agentRoleIcon = remember() {
            mutableStateOf<LocalImageData<*>?>(null)
        }.apply {
            value = remember(agentRole) {
                if (agentRole == null) return@remember null
                LocalImageData.Resource(
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
            LocalImageData.Resource(rankData?.getOrNull()?.let { competitiveRankIcon(it) } ?: 0)
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