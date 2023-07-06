package dev.flammky.valorantcompanion.pvp.http

class HttpResponseHeaders(private val contents: Map<String, List<String>>) {

    operator fun get(key: String): String? = contents[key]?.firstOrNull()

    fun getAll(key: String): List<String>? = contents[key]
}