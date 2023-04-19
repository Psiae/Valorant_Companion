package dev.flammky.valorantcompanion.auth.riot

interface UserInfoRequestSession {
    val firstException: Exception?
    val data: UserInfoRequestResponseData?
}