package dev.flammky.valorantcompanion.auth.ex

class CredentialExpiredException(
    override val message: String?
): Exception() {
}