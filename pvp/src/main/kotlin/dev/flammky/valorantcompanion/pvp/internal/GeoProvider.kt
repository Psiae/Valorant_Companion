package dev.flammky.valorantcompanion.pvp.internal

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

interface GeoProvider {

    fun get_shard(puuid: String): Result<RiotShard>
}