package dev.flammky.valorantcompanion.auth.riot

interface EntitlementRequestSession {
    val firstException: Exception?
    val data: EntitlementRequestResponseData?
}