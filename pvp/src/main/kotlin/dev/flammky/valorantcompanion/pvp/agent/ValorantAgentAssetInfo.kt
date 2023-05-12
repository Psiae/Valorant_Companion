package dev.flammky.valorantcompanion.pvp.agent

sealed class ValorantAgentAssetInfo(
    val isFullPortraitRightFacing: Boolean
) {

    //
    // yes, there are some questionable value
    //

    object ASTRA : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object BREACH : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object BRIMSTONE : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object CHAMBER : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object CYPHER : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = true
    )

    object FADE : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object GEKKO : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object HARBOR : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object JETT : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object KAYO : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object KILLJOY : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object NEON : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object OMEN : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object PHOENIX: ValorantAgentAssetInfo(
        isFullPortraitRightFacing = true
    )

    object RAZE : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object REYNA : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object SAGE : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = true
    )

    object SKYE : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object SOVA : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object VIPER : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    object YORU : ValorantAgentAssetInfo(
        isFullPortraitRightFacing = false
    )

    companion object {
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
    }
}