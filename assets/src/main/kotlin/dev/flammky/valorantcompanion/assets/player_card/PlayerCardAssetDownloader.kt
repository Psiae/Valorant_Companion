package dev.flammky.valorantcompanion.assets.player_card

import dev.flammky.valorantcompanion.assets.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PlayerCardAssetDownloader(
    private val httpClient: HttpClient
) {

    val coroutineScope = CoroutineScope(SupervisorJob())
}