package dev.flammky.valorantcompanion.assets.debug

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ex.AssetNotFoundException
import dev.flammky.valorantcompanion.assets.map.LoadMapImageRequest
import dev.flammky.valorantcompanion.assets.map.MapImageIdentifier
import dev.flammky.valorantcompanion.assets.player_card.LoadPlayerCardRequest
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardIdentifier
import dev.flammky.valorantcompanion.pvp.tier.CompetitiveRank
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.io.File

class DebugValorantAssetsLoaderClient(
    private val agentIconMapping: Map<String, LocalImage<*>>,
    private val roleIconMapping: Map<String, LocalImage<*>>,
    private val competitiveRankIconMapping: Map<CompetitiveRank, LocalImage<*>>,
    private val playerCardMapping: Map<PlayerCardIdentifier, LocalImage<*>>,
    private val mapImageMapping: Map<MapImageIdentifier, LocalImage<*>>
): ValorantAssetsLoaderClient {

    override fun loadMemoryCachedAgentIcon(agentId: String): LocalImage<*>? {
        return agentIconMapping[agentId]
    }

    override fun loadMemoryCachedRoleIcon(roleId: String): LocalImage<*>? {
        return roleIconMapping[roleId]
    }

    override fun loadMemoryCachedCompetitiveRankIcon(rank: CompetitiveRank): LocalImage<*>? {
        return competitiveRankIconMapping[rank]
    }

    override fun loadCompetitiveRankIconAsync(rank: CompetitiveRank): Deferred<Result<LocalImage<*>>> {
        return CompletableDeferred(
            value = competitiveRankIconMapping[rank]
                ?.let { Result.success(it) }
                ?: Result.failure(AssetNotFoundException())
        )
    }

    override fun loadAgentIconAsync(agentId: String): Deferred<Result<LocalImage<*>>> {
        return CompletableDeferred(
            value = agentIconMapping[agentId]
                ?.let { Result.success(it) }
                ?: Result.failure(AssetNotFoundException())
        )
    }

    override fun loadUserPlayerCardAsync(req: LoadPlayerCardRequest): Deferred<File> {
        TODO("Not yet implemented")
    }

    override fun loadMapImageAsync(req: LoadMapImageRequest): Deferred<Result<File>> {
        TODO("Not yet implemented")
    }

    override fun dispose() {
    }
}