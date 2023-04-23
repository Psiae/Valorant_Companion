package dev.flammky.valorantcompanion.auth.region

sealed class RiotShard(
    val assignedUrlName: String
) {
    object NA : RiotShard("na")

    object EU : RiotShard("eu")

    object APAC : RiotShard("ap")

    object KR : RiotShard("kr")

    fun getRegionShard(region: RiotRegion): RiotShard {
        return when(region) {
            RiotRegion.NA -> RiotShard.NA
            RiotRegion.LATAM -> RiotShard.NA
            RiotRegion.BR -> RiotShard.NA
            RiotRegion.EU -> RiotShard.EU
            RiotRegion.APAC -> RiotShard.APAC
            RiotRegion.KR -> RiotShard.KR
        }
    }
}