package dev.flammky.valorantcompanion.pvp.loadout

data class PlayerLoadout(
    val puuid: String,
    val version: Int,
    val guns: List<GunLoadout>,
    val sprays: List<SprayLoadout>,
    val identity: IdentityLoadout,
    val incognito: Boolean
)
