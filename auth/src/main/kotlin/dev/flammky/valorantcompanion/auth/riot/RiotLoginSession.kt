package dev.flammky.valorantcompanion.auth.riot

interface RiotLoginSession {
    val cookie: CookieRequestSession
    val auth: AuthRequestSession
    val entitlement: EntitlementRequestSession
    val userInfo: UserInfoRequestSession
    val ex: Exception?

    fun invokeOnCompletion(
        block: () -> Unit
    )
}