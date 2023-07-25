package dev.flammky.valorantcompanion.live.loadout.presentation.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LiveLoadoutContent(
    openSprayScreen: () -> Unit,
    openWeaponScreen: () -> Unit,
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        Column {
            Row {
                OpenWeaponScreenCard(
                    modifier = Modifier
                        .size(maxWidth / 2 - 8.dp)
                        .padding(start = 16.dp, top = 16.dp)
                        .clickable(onClick = openSprayScreen)
                )

                Spacer(modifier = Modifier.width(16.dp))

                OpenSprayScreenCard(
                    modifier = Modifier
                        .size(maxWidth / 2 - 8.dp)
                        .padding(end = 16.dp, top = 16.dp)
                        .clickable(onClick = openWeaponScreen)
                )
            }
        }
    }
}