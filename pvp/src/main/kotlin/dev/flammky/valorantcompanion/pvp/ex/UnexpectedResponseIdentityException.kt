package dev.flammky.valorantcompanion.pvp.ex

class UnexpectedResponseIdentityException : UnexpectedResponseException {

    internal constructor() : super()

    internal constructor(message: String) : super(message)

    internal constructor(message: String, cause: Throwable?) : super(message, cause)
}