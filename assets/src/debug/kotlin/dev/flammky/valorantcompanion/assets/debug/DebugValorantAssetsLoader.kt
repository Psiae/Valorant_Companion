package dev.flammky.valorantcompanion.assets.debug

import dev.flammky.valorantcompanion.assets.*
import dev.flammky.valorantcompanion.assets.map.MapImageIdentifier
import dev.flammky.valorantcompanion.assets.map.ValorantMapImageType
import dev.flammky.valorantcompanion.assets.spray.SprayImageIdentifier
import dev.flammky.valorantcompanion.assets.spray.SprayImageType
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentRole
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.collections.immutable.persistentMapOf

// asset service containing debug-only asset resource that might Not packaged onto release
class DebugValorantAssetService : ValorantAssetsService {

    override fun createLoaderClient(): ValorantAssetsLoaderClient {
        return DebugValorantAssetsLoaderClient(
            agentIconMapping = AGENT_ICON_MAPPING,
            roleIconMapping = ROLE_ICON_MAPPING,
            competitiveRankIconMapping = COMPETITIVE_RANK_MAPPING,
            // TODO
            playerCardMapping = emptyMap(),
            mapImageMapping = MAP_MAPPING,
            sprayImageMapping = SPRAY_MAPPING
        )
    }

    companion object {

        val AGENT_ICON_MAPPING = run {
            persistentMapOf<String, LocalImage<*>>().builder().apply {
                put(
                    ValorantAgentIdentity.ASTRA.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_astra_displayicon)
                )
                put(
                    ValorantAgentIdentity.BREACH.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_breach_displayicon)
                )
                put(
                    ValorantAgentIdentity.BRIMSTONE.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_brimstone_displayicon)
                )
                put(
                    ValorantAgentIdentity.CHAMBER.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_chamber_displayicon)
                )
                put(
                    ValorantAgentIdentity.CYPHER.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_cypher_displayicon)
                )
                put(
                    ValorantAgentIdentity.FADE.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_fade_displayicon)
                )
                put(
                    ValorantAgentIdentity.GEKKO.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_gekko_displayicon)
                )
                put(
                    ValorantAgentIdentity.HARBOR.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_harbor_displayicon)
                )
                put(
                    ValorantAgentIdentity.JETT.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_jett_displayicon)
                )
                put(
                    ValorantAgentIdentity.KAYO.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_kayo_displayicon)
                )
                put(
                    ValorantAgentIdentity.KILLJOY.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_killjoy_displayicon)
                )
                put(
                    ValorantAgentIdentity.NEON.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_neon_displayicon)
                )
                put(
                    ValorantAgentIdentity.OMEN.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_omen_displayicon)
                )
                put(
                    ValorantAgentIdentity.PHOENIX.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_phoenix_displayicon)
                )
                put(
                    ValorantAgentIdentity.RAZE.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_raze_displayicon)
                )
                put(
                    ValorantAgentIdentity.REYNA.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_reyna_displayicon)
                )
                put(
                    ValorantAgentIdentity.SAGE.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_sage_displayicon)
                )
                put(
                    ValorantAgentIdentity.SKYE.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_skye_displayicon)
                )
                put(
                    ValorantAgentIdentity.SOVA.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_sova_displayicon)
                )
                put(
                    ValorantAgentIdentity.VIPER.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_viper_displayicon)
                )
                put(
                    ValorantAgentIdentity.YORU.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.agent_yoru_displayicon)
                )
            }.build()
        }

        val ROLE_ICON_MAPPING = run {
            persistentMapOf<String, LocalImage<*>>().builder().apply {
                put(
                    ValorantAgentRole.Controller.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.role_controller_displayicon)
                )
                put(
                    ValorantAgentRole.Duelist.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.role_duelist_displayicon)
                )
                put(
                    ValorantAgentRole.Initiator.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.role_initiator_displayicon)
                )
                put(
                    ValorantAgentRole.Sentinel.uuid,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.role_sentinel_displayicon)
                )
            }.build()
        }

        val COMPETITIVE_RANK_MAPPING = run {
            persistentMapOf<CompetitiveRank, LocalImage<*>>().builder().apply {
                put(
                    CompetitiveRank.UNRANKED,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_unranked_smallicon)
                )
                put(
                    CompetitiveRank.UNUSED_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_unranked_smallicon)
                )
                put(
                    CompetitiveRank.UNUSED_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_unranked_smallicon)
                )
                put(
                    CompetitiveRank.IRON_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_iron1_smallicon)
                )
                put(
                    CompetitiveRank.IRON_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_iron2_smallicon)
                )
                put(
                    CompetitiveRank.IRON_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_iron3_smallicon)
                )
                put(
                    CompetitiveRank.BRONZE_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_bronze1_smallicon)
                )
                put(
                    CompetitiveRank.BRONZE_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_bronze2_smallicon)
                )
                put(
                    CompetitiveRank.BRONZE_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_bronze3_smallicon)
                )
                put(
                    CompetitiveRank.SILVER_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_silver1_smallicon)
                )
                put(
                    CompetitiveRank.SILVER_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_silver2_smallicon)
                )
                put(
                    CompetitiveRank.SILVER_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_silver3_smallicon)
                )
                put(
                    CompetitiveRank.GOLD_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_gold1_smallicon)
                )
                put(
                    CompetitiveRank.GOLD_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_gold2_smallicon)
                )
                put(
                    CompetitiveRank.GOLD_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_gold3_smallicon)
                )
                put(
                    CompetitiveRank.PLATINUM_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_platinum1_smallicon)
                )
                put(
                    CompetitiveRank.PLATINUM_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_platinum2_smallicon)
                )
                put(
                    CompetitiveRank.PLATINUM_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_platinum3_smallicon)
                )
                put(
                    CompetitiveRank.DIAMOND_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_diamond1_smallicon)
                )
                put(
                    CompetitiveRank.DIAMOND_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_diamond2_smallicon)
                )
                put(
                    CompetitiveRank.DIAMOND_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_diamond3_smallicon)
                )
                put(
                    CompetitiveRank.ASCENDANT_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_ascendant1_smallicon)
                )
                put(
                    CompetitiveRank.ASCENDANT_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_ascendant2_smallicon)
                )
                put(
                    CompetitiveRank.ASCENDANT_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_ascendant3_smallicon)
                )
                put(
                    CompetitiveRank.IMMORTAL_1,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_immortal1_smallicon)
                )
                put(
                    CompetitiveRank.IMMORTAL_2,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_immortal2_smallicon)
                )
                put(
                    CompetitiveRank.IMMORTAL_3,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_immortal3_smallicon)
                )
                put(
                    CompetitiveRank.IMMORTAL_MERGED,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_immortal3_smallicon)
                )
                put(
                    CompetitiveRank.RADIANT,
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.rank_radiant_smallicon)
                )
            }.build()
        }

        val MAP_MAPPING = run {
            persistentMapOf<MapImageIdentifier, LocalImage<*>>().builder().apply {
                put(
                    MapImageIdentifier(
                        puuid = ValorantMapIdentity.ASCENT.map_id,
                        type = ValorantMapImageType.ListView
                    ),
                    LocalImage.Resource(dev.flammky.valorantcompanion.assets.R.raw.ascent_listviewicon)
                )
            }.build()
        }

        val SPRAY_MAPPING = run {
            persistentMapOf<SprayImageIdentifier, LocalImage<*>>().builder().apply {
                put(
                    SprayImageIdentifier(
                        puuid = "nice_to_zap_you",
                        type = SprayImageType.FULL_ICON(transparentBackground = true)
                    ),
                    LocalImage.Resource(R_ASSET_RAW.debug_spray_nice_to_zap_you_transparent)
                )
            }.build()
        }
    }
}