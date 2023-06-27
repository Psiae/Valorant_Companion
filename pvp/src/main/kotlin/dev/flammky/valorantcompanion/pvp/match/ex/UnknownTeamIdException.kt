package dev.flammky.valorantcompanion.pvp.match.ex

import dev.flammky.valorantcompanion.pvp.party.ex.PVPModuleException

class UnknownTeamIdException(msg: String) : Exception(msg), PVPModuleException {
}