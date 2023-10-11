package dev.flammky.valorantcompanion.assets.weapon.gunbuddy

import kotlinx.coroutines.Deferred

interface ValorantGunBuddyAssetEndpoint {

   fun resolveImageUrlAsync(
      uuid: String
   ): Deferred<Result<String>>
}