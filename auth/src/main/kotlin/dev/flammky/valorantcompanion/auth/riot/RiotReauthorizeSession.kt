package dev.flammky.valorantcompanion.auth.riot

import kotlinx.coroutines.Job

interface RiotReauthorizeSession {

    fun asCoroutineJob(): Job
}