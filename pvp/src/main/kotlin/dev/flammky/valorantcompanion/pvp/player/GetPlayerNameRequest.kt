package dev.flammky.valorantcompanion.pvp.player

import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import kotlinx.collections.immutable.ImmutableMap

class GetPlayerNameRequest(
    // the shard of this request, if null then use the signed in user
    val shard: RiotShard?,
    val signedInUserPUUID: String,
    val lookupPUUIDs: List<String>
) {
}

class GetPlayerNameRequestResult(
    private val map: ImmutableMap<String, Result<PlayerPVPName>>,
    val ex: Exception?
): ImmutableMap<String, Result<PlayerPVPName>> by map {}