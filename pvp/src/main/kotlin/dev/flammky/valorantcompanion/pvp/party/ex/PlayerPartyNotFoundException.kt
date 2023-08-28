package dev.flammky.valorantcompanion.pvp.party.ex

import dev.flammky.valorantcompanion.pvp.ex.PVPModuleException

class PlayerPartyNotFoundException internal constructor() : Exception(), PVPModuleException {
}