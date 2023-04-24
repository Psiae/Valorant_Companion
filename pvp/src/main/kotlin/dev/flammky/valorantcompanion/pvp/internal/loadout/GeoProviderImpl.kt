package dev.flammky.valorantcompanion.pvp.internal.loadout

import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider

class GeoProviderImpl(
    private val geo: RiotGeoRepository
) : GeoProvider {

    override fun get_shard(puuid: String): Result<RiotShard> {
        return runCatching { geo.getGeoShardInfo(puuid)?.shard ?: TODO() }
    }
}