package dev.flammky.valorantcompanion.auth.riot

interface RegionInfoRequestSession {
    val firstException: Exception?
    val data: RegionInfoRequestResponseData?
}