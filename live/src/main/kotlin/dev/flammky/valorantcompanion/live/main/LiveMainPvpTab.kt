package dev.flammky.valorantcompanion.live.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceContentColorAsState

@Composable
internal fun LiveMainPvpTab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Box(modifier = modifier) {
    Icon(
        modifier = Modifier
            .size(42.dp)
            .clickable(onClick = onClick)
            .padding(10.dp)
            .align(Alignment.Center)
        ,
        painter = painterResource(
            id = R_ASSET_DRAWABLE.battle_ios_glyph_100px
        ),
        contentDescription = "PVP",
        tint = Material3Theme.surfaceContentColorAsState().value
    )
}