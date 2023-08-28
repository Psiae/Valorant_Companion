package dev.flammky.valorantcompanion.pvp.ex

class RankNotPresentInSeasonException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause), PVPModuleException {}