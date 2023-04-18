package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.*
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotAuthenticatedAccount
import kotlinx.coroutines.Deferred

internal class RiotAuthRepositoryImpl() : RiotAuthRepository {

    override var activeAccount: RiotAuthenticatedAccount? = null
        private set

}