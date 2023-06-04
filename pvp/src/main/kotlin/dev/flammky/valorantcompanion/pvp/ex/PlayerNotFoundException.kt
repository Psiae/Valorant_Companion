package dev.flammky.valorantcompanion.pvp.ex

import dev.flammky.valorantcompanion.pvp.party.ex.PVPModuleException

class PlayerNotFoundException internal constructor() : Exception(), PVPModuleException {
}