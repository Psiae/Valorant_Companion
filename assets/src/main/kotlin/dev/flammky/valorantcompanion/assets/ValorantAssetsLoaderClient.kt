package dev.flammky.valorantcompanion.assets

import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.coroutines.Deferred
import java.io.File

interface ValorantAssetsLoaderClient {

    fun loadMemoryCachedAgentIcon(
        agentId: String
    ): LocalImage<*>?

    fun loadMemoryCachedRoleIcon(
        roleId: String
    ): LocalImage<*>?

    fun loadMemoryCachedCompetitiveRankIcon(
        rank: CompetitiveRank
    ): LocalImage<*>?

    fun loadCompetitiveRankIconAsync(
        rank: CompetitiveRank
    ): Deferred<Result<LocalImage<*>>>

    fun loadAgentIconAsync(
        agentId: String
    ): Deferred<Result<LocalImage<*>>>

    fun loadUserPlayerCardAsync(
        req: LoadPlayerCardRequest
    ): Deferred<File>

    fun loadMapImageAsync(
        req: LoadMapImageRequest
    ): Deferred<Result<File>>

    fun dispose()
}