package dev.flammky.valorantcompanion.assets.internal

import android.util.Log
import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.bundle.ValorantBundleAssetDownloader
import dev.flammky.valorantcompanion.assets.ktor.KtorWrappedHttpClient
import dev.flammky.valorantcompanion.assets.map.ValorantApiMapAssetEndpoint
import dev.flammky.valorantcompanion.assets.map.ValorantMapAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.PlayerCardAssetDownloader
import dev.flammky.valorantcompanion.assets.player_card.ValorantApiPlayerCardAssetEndpoint
import dev.flammky.valorantcompanion.assets.spray.KtxValorantSprayAssetSerializer
import dev.flammky.valorantcompanion.assets.spray.ValorantApiSprayAssetEndpoint
import dev.flammky.valorantcompanion.assets.spray.ValorantApiSprayResponseParser
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayAssetDownloader
import dev.flammky.valorantcompanion.assets.valorantapi.bundle.ValorantApiBundleAssetEndpoint
import dev.flammky.valorantcompanion.assets.valorantapi.bundle.ValorantApiBundleAssetParser
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.logging.*

class ValorantAssetsServiceImpl(
    private val repo: ValorantAssetRepository
) : ValorantAssetsService {

    private val httpClient by lazy {
        KtorWrappedHttpClient(
            self = HttpClient(OkHttp) {
                if (BuildConfig.DEBUG) {
                    install(Logging) {
                        logger = object : Logger {
                            override fun log(message: String) {
                                Log.i("ASSET_KtorHttpClient", message)
                            }
                        }
                        level = LogLevel.ALL
                    }
                }
            }
        )
    }

    override fun createLoaderClient(): ValorantAssetsLoaderClient {
        return DisposableValorantAssetsLoaderClient(
            repository = repo,
            player_card_downloader = PlayerCardAssetDownloader(
                httpClient,
                ValorantApiPlayerCardAssetEndpoint()
            ),
            map_asset_downloader = ValorantMapAssetDownloader(
                httpClient,
                ValorantApiMapAssetEndpoint()
            ),
            spray_asset_downloader = ValorantSprayAssetDownloader(
                httpClient,
                ValorantApiSprayAssetEndpoint(),
                ValorantApiSprayResponseParser(KtxValorantSprayAssetSerializer())
            ),
            bundle_asset_downloader = ValorantBundleAssetDownloader(
                httpClient,
                ValorantApiBundleAssetEndpoint(),
                ValorantApiBundleAssetParser()
            )
        )
    }
}