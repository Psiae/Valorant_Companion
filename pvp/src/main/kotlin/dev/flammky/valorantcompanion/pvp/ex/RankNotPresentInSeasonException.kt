package dev.flammky.valorantcompanion.pvp.ex

import dev.flammky.valorantcompanion.pvp.party.ex.PVPModuleException
import kotlin.coroutines.cancellation.CancellationException

class RankNotPresentInSeasonException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause), PVPModuleException {}