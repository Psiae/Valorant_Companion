package dev.flammky.valorantcompanion.assets.valorantapi.gunbuddy

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.weapon.gunbuddy.ValorantGunBuddyAssetEndpoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class ValorantApiGunBuddyAssetEndpoint(
    /*private val httpClientFactory: () -> AssetHttpClient*/
) : ValorantGunBuddyAssetEndpoint {

    override fun resolveImageUrlAsync(
        uuid: String
    ): Deferred<Result<String>> {
        // TODO: dynamic resolve URL
        return CompletableDeferred(Result.success("$BUDDY_MEDIA_URL/$uuid/displayicon.png"))
    }

    companion object {
        val BASE_URL = "https://valorant-api.com"
        val BASE_MEDIA_URL = "https://media.valorant-api.com"

        val BUDDY_URL = "$BASE_URL/v1/buddies"
        val BUDDY_MEDIA_URL = "$BASE_MEDIA_URL/buddies/"
    }
}