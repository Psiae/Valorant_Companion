package dev.flammky.valorantcompanion.live.loadout.presentation.root.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.live.loadout.presentation.root.OpenSprayScreenCard

@Composable
@Preview
private fun OpenSprayScreenCardPreview(

) {
    DefaultMaterial3Theme(
        dark = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Material3Theme.backgroundColorAsState().value)
        ) {
            OpenSprayScreenCard(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.42f)
                    .aspectRatio(1f)
                    .clickable {  },
                iconModifier = Modifier,
                textModifier = Modifier
            )
        }
    }
}