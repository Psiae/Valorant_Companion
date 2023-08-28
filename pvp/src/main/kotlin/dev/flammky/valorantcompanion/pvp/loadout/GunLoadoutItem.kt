package dev.flammky.valorantcompanion.pvp.loadout

data class GunLoadoutItem(
    val id: String,
    val skinId: String,
    val skinLevelId: String,
    val chromaId: String,
    val charmInstanceId: String?,
    val charmId: String?,
    val charmLevelId: String?,
    // TODO: figure out
    val attachements: List<Any>
)