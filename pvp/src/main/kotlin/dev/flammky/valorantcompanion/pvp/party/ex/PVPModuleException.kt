package dev.flammky.valorantcompanion.pvp.party.ex

interface PVPModuleException {

    fun asJavaException(): Exception = this as Exception
}