package dev.flammky.valorantcompanion.pvp.agent

sealed class ValorantAgentRole(
    val uuid: String,
    val displayName: String,
    val codeName: String,
    val description: String
) {

    object Controller : ValorantAgentRole(
        uuid = "4ee40330-ecdd-4f2f-98a8-eb1243428373",
        displayName = "Controller",
        codeName = "Strategist",
        description = buildString {
            append("Controllers are experts in slicing up dangerous territory to set their team up ")
            append("for success.")
        }
    )

    object Duelist : ValorantAgentRole(
        uuid = "dbe8757e-9e92-4ed4-b39f-9dfc589691d4",
        displayName = "Duelist",
        codeName = "Assault",
        description = buildString {
            append("Duelists are self-sufficient fraggers who their team expects, ")
            append("through abilities and skills, to get high frags and seek out engagements first.")
        }
    )

    object Sentinel : ValorantAgentRole(
        uuid = "5fc02f99-4091-4486-a531-98459a3e95e9",
        displayName = "Sentinel",
        "Sentinel",
        description = buildString {
            append("Sentinels are defensive experts who can lock down areas and watch flanks, ")
            append("both on attacker and defender rounds.")
        }
    )

    object Initiator : ValorantAgentRole(
        uuid = "1b47567f-8f7b-444b-aae3-b0c634622d10",
        displayName = "Initiator",
        codeName = "Breaker",
        description = buildString {
            append("Initiators challenge angles by setting up their team to enter contested ground ")
            append("and push defenders away.")
        }
    )
}
