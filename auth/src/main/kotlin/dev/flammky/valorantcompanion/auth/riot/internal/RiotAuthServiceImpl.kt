package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*

internal class RiotAuthServiceImpl(
    private val repository: RiotAuthRepositoryImpl
) : RiotAuthService {

    override fun createLoginClient(): RiotLoginClient = KtorRiotLoginClient(repository)
}