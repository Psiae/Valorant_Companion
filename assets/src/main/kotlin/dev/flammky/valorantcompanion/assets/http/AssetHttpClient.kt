package dev.flammky.valorantcompanion.assets.http

abstract class AssetHttpClient() {

    abstract suspend fun get(
        url: String,
        sessionHandler: AssetHttpSessionHandler
    )
}