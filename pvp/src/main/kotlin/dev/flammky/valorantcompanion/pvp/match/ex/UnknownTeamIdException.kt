package dev.flammky.valorantcompanion.pvp.match.ex

import dev.flammky.valorantcompanion.pvp.ex.PVPModuleException

class UnknownTeamIdException(msg: String) : Exception(msg), PVPModuleException {
}