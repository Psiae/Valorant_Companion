package dev.flammky.valorantcompanion.pvp.ex

interface PVPModuleException {

    fun asJavaException(): Exception = this as java.lang.Exception
}