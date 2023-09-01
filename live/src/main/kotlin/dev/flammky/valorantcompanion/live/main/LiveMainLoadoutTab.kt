package dev.flammky.valorantcompanion.live.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
internal fun LiveMainLoadoutTab(
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
            id = R_ASSET_DRAWABLE.tx_icon_collection,
        ),
        contentDescription = "LOADOUT",
        tint = Material3Theme.surfaceContentColorAsState().value
    )
}