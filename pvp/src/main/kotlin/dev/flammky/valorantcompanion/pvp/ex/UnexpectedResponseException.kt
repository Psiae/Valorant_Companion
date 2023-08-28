package dev.flammky.valorantcompanion.pvp.ex

open class UnexpectedResponseException : Exception, PVPModuleException {

    internal constructor() : super()

    internal constructor(message: String) : super(message)

    internal constructor(message: String, cause: Throwable?) : super(message, cause)
}