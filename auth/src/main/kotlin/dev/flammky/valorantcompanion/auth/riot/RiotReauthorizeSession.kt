package dev.flammky.valorantcompanion.auth.riot

import kotlinx.coroutines.Job

interface RiotReauthorizeSession {

    val success: Boolean
    fun asCoroutineJob(): Job
}