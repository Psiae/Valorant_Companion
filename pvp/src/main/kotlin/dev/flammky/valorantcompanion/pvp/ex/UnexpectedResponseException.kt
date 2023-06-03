package dev.flammky.valorantcompanion.pvp.ex

import dev.flammky.valorantcompanion.pvp.party.ex.PVPModuleException

open class UnexpectedResponseException internal constructor(
    override val message: String?
) : Exception(), PVPModuleException {
}