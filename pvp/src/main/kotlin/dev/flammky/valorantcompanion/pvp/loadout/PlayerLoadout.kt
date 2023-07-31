package dev.flammky.valorantcompanion.pvp.loadout

data class PlayerLoadout(
    val puuid: String,
    val version: Int,
    val guns: List<GunLoadoutItem>,
    val sprays: List<SprayLoadoutItem>,
    val identity: IdentityLoadout,
    val incognito: Boolean
)
