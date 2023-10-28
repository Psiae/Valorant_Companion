package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.*
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.auth.riot.RiotAuthenticatedAccount

internal class RiotAuthRepositoryImpl() : RiotAuthRepository {

    private val registry = UserAccountRegistry()

    override val activeAccount: AuthenticatedAccount?
        get() = registry.activeAccount

    override fun registerAuthenticatedAccount(
        account: RiotAuthenticatedAccount,
        setActive: Boolean
    ) {
        registry.registerAuthenticatedAccount(account, setActive)
    }

    override fun setActiveAccount(puuid: String) {
        registry.setActiveAccount(puuid)
    }

    override fun registerActiveAccountChangeListener(handler: ActiveAccountListener) {
        registry.registerActiveAccountListener(handler)
    }

    override fun unRegisterActiveAccountListener(handler: ActiveAccountListener) {
        registry.unRegisterActiveAccountListener(handler)
    }

    fun updateEntitlement(id: String, token: String) {
        registry.updateEntitlementToken(id, token)
    }

    fun updateIdToken(id: String, token: String) {
        registry.updateIdToken(id, token)
    }

    fun updateAccessToken(id: String, token: String) {
        registry.updateAccessToken(id, token)
    }

    fun updateSSID(id: String, ssid: String) {
        Log.d("ValorantCompanion_DEBUG", "updateSSID($id, $ssid)")
        registry.updateSSID(id, ssid)
    }

    fun getEntitlementToken(id: String): String? {
        return registry.getEntitlementToken(id)
    }

    fun getIdToken(id: String): String? {
        return registry.getIdToken(id)
    }

    fun getAccessToken(id: String): String? {
        return registry.getAccessToken(id)
    }

    fun getSSID(id: String): String? {
        return registry.getSSID(id)
    }
}