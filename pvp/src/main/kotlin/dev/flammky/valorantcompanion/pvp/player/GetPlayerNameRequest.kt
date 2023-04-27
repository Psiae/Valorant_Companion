package dev.flammky.valorantcompanion.pvp.player

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

class GetPlayerNameRequest(
    val shard: RiotShard,
    val lookupPUUIDs: List<String>
) {
}

class GetPlayerNameRequestResult(
    private val map: Map<String, PlayerPvpName>
) {
    operator fun get(puuid: String): PlayerPvpName? = map[puuid]
}