package dev.flammky.valorantcompanion.pvp.loadout

data class GunLoadout(
    val id: String,
    val skinId: String,
    val skinLevelId: String,
    val chromaId: String,
    val charmInstanceId: String?,
    val charmId: String?,
    val charmLevelId: String?,
    val attachements: List<Any>
)