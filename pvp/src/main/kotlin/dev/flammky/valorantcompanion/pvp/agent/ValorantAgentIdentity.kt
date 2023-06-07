package dev.flammky.valorantcompanion.pvp.agent

import dev.flammky.valorantcompanion.pvp.util.mapSealedObjectInstancesToPersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass

sealed class ValorantAgentIdentity(
    val uuid: String,
    val displayName: String,
    val codeName: String,
    val role: ValorantAgentRole,
    val description: String
)  {

    object ASTRA : ValorantAgentIdentity(
        uuid = "41fb69c1-4189-7b37-f117-bcaf1e96f1bf",
        displayName = "Astra",
        codeName = "Rift",
        role = ValorantAgentRole.Controller,
        description = buildString {
            append("Ghanaian Agent Astra harnesses the energies of the cosmos to reshape ")
            append("battlefields to her whim. With full command of her astral form and a talent ")
            append("for deep strategic foresight, she's always eons ahead of her enemy's next move.")
        }
    )

    object BREACH : ValorantAgentIdentity(
        uuid = "5f8d3a7f-467b-97f3-062c-13acf203c006",
        displayName = "Breach",
        codeName = "Breach",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("The bionic Swede Breach fires powerful, ")
            append("targeted kinetic blasts to aggressively clear a path through enemy ground. ")
            append("The damage and disruption he inflicts ensures no fight is ever fair.")
        }
    )

    object BRIMSTONE : ValorantAgentIdentity(
        "9f0d8ba9-4140-b941-57d3-a7ad57c6b417",
        "Brimstone",
        "Sarge",
        role = ValorantAgentRole.Controller,
        description = buildString {
            append("Joining from the USA, Brimstone's orbital arsenal ensures his squad always has ")
            append("the advantage. His ability to deliver utility precisely and safely make him ")
            append("the unmatched boots-on-the-ground commander.  ")
        }
    )

    object CHAMBER : ValorantAgentIdentity(
        uuid = "22697a3d-45bf-8dd7-4fec-84a9e28c69d7",
        displayName = "Chamber",
        codeName = "Deadeye",
        role = ValorantAgentRole.Sentinel,
        description = buildString {
            append("Well dressed and well armed, ")
            append("French weapons designer Chamber expels aggressors with deadly precision. ")
            append("He leverages his custom arsenal to hold the line and pick off enemies from afar, ")
            append("with a contingency built for every plan.")
        }
    )

    object CYPHER : ValorantAgentIdentity(
        uuid = "117ed9e3-49f3-6512-3ccf-0cada7e3823b",
        displayName = "Cypher",
        codeName = "Gumshoe",
        role = ValorantAgentRole.Sentinel,
        description = buildString {
            append("The Moroccan information broker, ")
            append("Cypher is a one-man surveillance network who keeps tabs on the enemy's every move. ")
            append("No secret is safe. No maneuver goes unseen. Cypher is always watching.")
        }
    )

    object FADE : ValorantAgentIdentity(
        uuid = "dade69b4-4f5a-8528-247b-219e5a1facd6",
        displayName = "Fade",
        codeName = "BountyHunter",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("Turkish bounty hunter Fade unleashes the power of raw nightmare ")
            append("to seize enemy secrets. Attuned with terror itself, she hunts down targets and ")
            append("reveals their deepest fears - before crushing them in the dark.")
        }
    )

    object GEKKO : ValorantAgentIdentity(
        uuid = "e370fa57-4757-3604-3648-499e1f642d3f",
        displayName = "Gekko",
        codeName = "Aggrobot",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("Gekko the Angeleno leads a tight-knit crew of calamitous creatures. ")
            append("His buddies bound forward, ")
            append("scattering enemies out of the way, ")
            append("with Gekko chasing them down to regroup and go again.")
        }
    )

    object HARBOR : ValorantAgentIdentity(
        uuid = "95b78ed7-4637-86d9-7e41-71ba8c293152",
        displayName = "Harbor",
        codeName = "Mage",
        role = ValorantAgentRole.Controller,
        description = buildString {
            append("Hailing from India’s coast, Harbor storms the field wielding ancient technology ")
            append("with dominion over water. He unleashes frothing rapids and crushing waves to ")
            append("shield his allies and to pummel those that oppose him.")
        }
    )

    object JETT : ValorantAgentIdentity(
        "add6443a-41bd-e414-f6ad-e58d267f4e95",
        displayName = "Jett",
        codeName = "Wushu",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Representing her home country of South Korea, Jett's agile and evasive fighting ")
            append("style lets her take risks no one else can. She runs circles around every skirmish, ")
            append("cutting enemies up before they even know what hit them.")
        }
    )

    object KAYO : ValorantAgentIdentity(
        uuid = "601dbbe7-43ce-be57-2a40-4abd24953621",
        displayName = "KAY/O",
        codeName = "Grenadier",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("KAY/O is a machine of war built for a single purpose: neutralizing radiants. ")
            append("His power to Suppress enemy abilities cripples his opponents' capacity to fight back, ")
            append("securing him and his allies the ultimate edge.")
        }
    )

    object KILLJOY : ValorantAgentIdentity(
        uuid = "1e58de9c-4950-5125-93e9-a0aee9f98746",
        displayName = "Killjoy",
        codeName = "Killjoy",
        role = ValorantAgentRole.Sentinel,
        description = buildString {
            append("The genius of Germany, Killjoy secures and defends key battlefield positions ")
            append("with a collection of traps, turrets, and mines. ")
            append("Each invention is primed to punish any assailant too dumb to back down.")
        }
    )

    object NEON : ValorantAgentIdentity(
        uuid = "bb2a4828-46eb-8cd1-e765-15848195d751",
        displayName = "Neon",
        codeName = "Sprinter",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Filipino Agent Neon surges forward at shocking speeds, ")
            append("discharging bursts of bioelectric radiance as fast as her body generates it. ")
            append("She races ahead to catch enemies off guard ")
            append("then strikes them down quicker than lightning.")
        }
    )

    object OMEN : ValorantAgentIdentity(
        uuid = "8e253930-4c05-31dd-1b6c-968525494517",
        displayName = "Omen",
        codeName = "Wraith",
        role = ValorantAgentRole.Controller,
        description = buildString {
            append("A phantom of a memory, Omen hunts in the shadows. He renders enemies blind, ")
            append("teleports across the field, then lets paranoia take hold as his foe scrambles to ")
            append("uncover where he might strike next.")
        }
    )

    object PHOENIX : ValorantAgentIdentity(
        uuid = "eb93336a-449b-9c1b-0a54-a891f7921d69",
        displayName = "Phoenix",
        codeName = "Phoenix",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Hailing from the U.K., Phoenix's star power shines through in his fighting style, ")
            append("igniting the battlefield with flash and flare. Whether he's got backup or not, ")
            append("he's rushing in to fight on his own terms.")
        }
    )

    object RAZE : ValorantAgentIdentity(
        uuid = "f94c3b30-42be-e959-889c-5aa313dba261",
        displayName = "Raze",
        codeName = "Clay",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Raze explodes out of Brazil with her big personality and big guns. ")
            append("With her blunt-force-trauma playstyle, ")
            append("she excels at flushing entrenched enemies and clearing tight spaces ")
            append("with a generous dose of \"boom\".")
        }
    )

    object REYNA : ValorantAgentIdentity(
        uuid = "a3bfb853-43b2-7238-a4f1-ad90e9e46bcc",
        displayName = "Reyna",
        codeName = "Vampire",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Forged in the heart of Mexico, Reyna dominates single combat, popping off with ")
            append("each kill she scores. Her capability is only limited by her raw skill, ")
            append("making her sharply dependant on performance. ")
        }
    )

    object SAGE : ValorantAgentIdentity(
        uuid = "569fdd95-4d10-43ab-ca70-79becc718b46",
        displayName = "Sage",
        codeName = "Thorne",
        role = ValorantAgentRole.Sentinel,
        description = buildString {
            append("The bastion of China, Sage creates safety for herself and her team wherever ")
            append("she goes. Able to revive fallen friends and stave off forceful assaults, ")
            append("she provides a calm center to a hellish battlefield.")
        }
    )

    object SKYE : ValorantAgentIdentity(
        uuid = "6f2a04ca-43e0-be17-7f36-b3908627744d",
        displayName = "Skye",
        codeName = "Guide",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("Hailing from Australia, ")
            append("Skye and her band of beasts trailblaze the way through hostile territory. ")
            append("With her creations hampering the enemy, ")
            append("and her power to heal others, the team is strongest and safest by Skye's side.")
        }
    )

    object SOVA : ValorantAgentIdentity(
        uuid = "320b2a48-4d9b-a075-30f1-1f93a9b638fa",
        displayName = "Sova",
        codeName = "Hunter",
        role = ValorantAgentRole.Initiator,
        description = buildString {
            append("Born from the eternal winter of Russia's tundra, Sova tracks, finds, and eliminates ")
            append("enemies with ruthless efficiency and precision. ")
            append("His custom bow and incredible scouting abilities ensure that even if you run, you cannot hide.")
        }
    )

    object VIPER : ValorantAgentIdentity(
        uuid = "707eab51-4836-f488-046a-cda6bf494859",
        displayName = "Viper",
        codeName = "Pandemic",
        role = ValorantAgentRole.Controller,
        description = buildString {
            append("The American Chemist, Viper deploys an array of poisonous chemical devices to ")
            append("control the battlefield and cripple the enemy's vision. If the toxins don't ")
            append("kill her prey, her mindgames surely will.")
        }
    )

    object YORU : ValorantAgentIdentity(
        uuid = "7f94d92c-4234-0a36-9646-3a87eb8b5c89",
        displayName = "Yoru",
        codeName = "Stealth",
        role = ValorantAgentRole.Duelist,
        description = buildString {
            append("Japanese native Yoru rips holes straight through reality to infiltrate enemy ")
            append("lines unseen. Using deception and aggression in equal measure, he gets the ")
            append("drop on each target before they know where to look.")
        }
    )

    companion object {

        private val SubclassesInstance by lazy {
            ValorantAgentIdentity::class.mapSealedObjectInstancesToPersistentList()
        }

        fun of(agent: ValorantAgent) = when(agent) {
            ValorantAgent.ASTRA -> ASTRA
            ValorantAgent.BREACH -> BREACH
            ValorantAgent.BRIMSTONE -> BRIMSTONE
            ValorantAgent.CHAMBER -> CHAMBER
            ValorantAgent.CYPHER -> CYPHER
            ValorantAgent.FADE -> FADE
            ValorantAgent.GEKKO -> GEKKO
            ValorantAgent.HARBOR -> HARBOR
            ValorantAgent.JETT -> JETT
            ValorantAgent.KAYO -> KAYO
            ValorantAgent.KILLJOY -> KILLJOY
            ValorantAgent.NEON -> NEON
            ValorantAgent.OMEN -> OMEN
            ValorantAgent.PHOENIX -> PHOENIX
            ValorantAgent.RAZE -> RAZE
            ValorantAgent.REYNA -> REYNA
            ValorantAgent.SAGE -> SAGE
            ValorantAgent.SKYE -> SKYE
            ValorantAgent.SOVA -> SOVA
            ValorantAgent.VIPER -> VIPER
            ValorantAgent.YORU -> YORU
        }

        fun ofID(id: String): ValorantAgentIdentity? {
            return SubclassesInstance.find { it.uuid == id }
        }

        fun iter(): Iterator<ValorantAgentIdentity> = SubclassesInstance.iterator()
        fun asList(): List<ValorantAgentIdentity> = SubclassesInstance
    }
}
