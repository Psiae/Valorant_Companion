package dev.flammky.valorantcompanion.auth.riot

interface RiotLoginSession {
    val authException: Exception?
    val cookieException: Exception?
    val completionException: Exception?

    fun invokeOnCompletion(
        block: () -> Unit
    )
}