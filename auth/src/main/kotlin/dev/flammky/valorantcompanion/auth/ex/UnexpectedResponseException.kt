package dev.flammky.valorantcompanion.auth.ex

class UnexpectedResponseException(
    override val message: String?
) : Exception() {
}