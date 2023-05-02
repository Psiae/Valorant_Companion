package dev.flammky.valorantcompanion.pvp.player

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

class GetPlayerNameRequest(
    val shard: RiotShard,
    val lookupPUUIDs: List<String>
) {
}

class GetPlayerNameRequestResult(
    private val map: Map<String, Result<PlayerPvpName>>
): Map<String, Result<PlayerPvpName>> by map {}