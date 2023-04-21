package dev.flammky.valorantcompanion.assets.internal

import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.ValorantApiPlayerCardEndpoints
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.logging.*

class ValorantAssetsServiceImpl(
    private val repo: ValorantAssetRepository
) : ValorantAssetsService {

    override fun createLoaderClient(): ValorantAssetsLoaderClient {
        return DisposableValorantAssetsLoaderClient(
            repository = repo,
            player_card_downloader = PlayerCardAssetDownloader(
                KtorWrappedHttpClient(createKtorHttpClient()),
                ValorantApiPlayerCardEndpoints()
            )
        )
    }

    fun createKtorHttpClient() = HttpClient(OkHttp) {
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.i("KtorHttpClient", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
}