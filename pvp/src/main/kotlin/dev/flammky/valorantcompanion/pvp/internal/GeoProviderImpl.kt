package dev.flammky.valorantcompanion.pvp.internal

import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

class GeoProviderImpl(
    private val geo: RiotGeoRepository
) : GeoProvider {

    override fun get_shard(puuid: String): Result<RiotShard> {
        return runCatching { geo.getGeoShardInfo(puuid)?.shard ?: TODO() }
    }
}