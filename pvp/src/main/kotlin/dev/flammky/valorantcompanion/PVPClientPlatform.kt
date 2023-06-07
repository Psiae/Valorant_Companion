package dev.flammky.valorantcompanion

import kotlinx.serialization.json.Json

object PVPClientPlatform {

    val JSON_STRING by lazy {
        """
            {
                "platformType": "PC",
                "platformOS": "Windows",
                "platformOSVersion": "10.0.19045.1.256.64bit",
                "platformChipset": "Unknown"
            }
        """.trimIndent()
    }
    val BASE_64 by lazy {
        "ewogICAgInBsYXRmb3JtVHlwZSI6ICJQQyIsCiAgICAicGxhdGZvcm1PUyI6ICJXaW5kb3dzIiwKICAgICJwbGF0" +
                "Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQ1LjEuMjU2LjY0Yml0IiwKICAgICJwbGF0Zm9ybUNoaXBzZXQiOiAi" +
                "VW5rbm93biIKfQ=="
    }
}