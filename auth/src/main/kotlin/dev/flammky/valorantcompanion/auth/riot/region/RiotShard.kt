package dev.flammky.valorantcompanion.auth.riot.region

sealed class RiotShard(
    val assignedUrlName: String
) {
    object NA : RiotShard("na")

    object EU : RiotShard("eu")

    object APAC : RiotShard("ap")

    object KR : RiotShard("kr")

    companion object {
        fun ofRegion(region: RiotRegion): RiotShard {
            return when(region) {
                RiotRegion.NA -> NA
                RiotRegion.LATAM -> NA
                RiotRegion.BR -> NA
                RiotRegion.EU -> EU
                RiotRegion.APAC -> APAC
                RiotRegion.KR -> KR
            }
        }
    }
}