package dev.flammky.valorantcompanion.pvp.player

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

class GetPlayerNameRequest(
    val shard: RiotShard,
    val signedInUserPUUID: String,
    val lookupPUUIDs: List<String>
) {
}

class GetPlayerNameRequestResult(
    private val map: Map<String, Result<PlayerPVPName>>,
    val ex: Exception?
): Map<String, Result<PlayerPVPName>> by map {}