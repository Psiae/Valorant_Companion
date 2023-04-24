package dev.flammky.valorantcompanion.career.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun MainCareer() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    with(LocalDensity.current) {
                        WindowInsets.statusBars
                            .getTop(this)
                            .toDp() + 20.dp
                    }
                )
        )
        PlayerProfileCard(
            state = rememberPlayerProfileCardPresenter().present()
        )
        CareerSectionTab()
    }
}