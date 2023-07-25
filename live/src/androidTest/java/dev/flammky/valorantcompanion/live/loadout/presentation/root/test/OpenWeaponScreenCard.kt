package dev.flammky.valorantcompanion.live.loadout.presentation.root.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.live.loadout.presentation.root.OpenWeaponScreenCard
import kotlin.math.ln

@Composable
@Preview
private fun OpenWeaponScreenCardPreview(

) {
    DefaultMaterial3Theme(
        dark = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Material3Theme.backgroundColorAsState().value)
        ) {
            OpenWeaponScreenCard(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.42f)
                    .aspectRatio(1f)
                    .clickable {  }
            )
        }
    }
}