package dev.flammky.valorantcompanion.pvp.agent

import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.ReflectProperties.Val

sealed class ValorantAgent {

    object ASTRA : ValorantAgent()

    object BREACH : ValorantAgent()

    object BRIMSTONE : ValorantAgent()

    object CHAMBER : ValorantAgent()

    object CYPHER : ValorantAgent()

    object FADE : ValorantAgent()

    object GEKKO : ValorantAgent()

    object HARBOR : ValorantAgent()

    object JETT : ValorantAgent()

    object KAYO : ValorantAgent()

    object KILLJOY : ValorantAgent()

    object NEON : ValorantAgent()

    object OMEN : ValorantAgent()

    object PHOENIX : ValorantAgent()

    object RAZE : ValorantAgent()

    object REYNA : ValorantAgent()

    object SAGE : ValorantAgent()

    object SKYE : ValorantAgent()

    object SOVA : ValorantAgent()

    object VIPER : ValorantAgent()

    object YORU : ValorantAgent()

    companion object {

        private val SUBCLASSES by lazy {
            ValorantAgent::class.sealedSubclasses.mapNotNullTo(
                destination = persistentListOf<ValorantAgent>().builder(),
                transform = KClass<out ValorantAgent>::objectInstance
            )
        }

        fun iter(): Iterator<ValorantAgent> = SUBCLASSES.iterator()
        fun asList(): List<ValorantAgent> = SUBCLASSES
    }
}
