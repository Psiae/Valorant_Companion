package dev.flammky.valorantcompanion.auth.riot

data class RiotLoginRequest(
    val username: String,
    val password: String,
) {
}

data class RiotLoginRequestResult(
    val authEx: Exception? = null,
    val responseEx: Exception? = null
) {

}