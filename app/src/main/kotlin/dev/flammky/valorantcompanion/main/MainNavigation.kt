package dev.flammky.valorantcompanion.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.career.main.CareerMain
import dev.flammky.valorantcompanion.live.LiveMain
import kotlin.math.ln
import androidx.compose.material3.Scaffold as Material3Scaffold
import androidx.compose.material3.Surface as Material3Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    state: MainNavigationState
) {
    Material3Scaffold(
        bottomBar = { BottomBar(state) },
        containerColor = Material3Theme.backgroundColorAsState().value
    ) { contentPadding ->
        NavigationHost(state.navController as NavHostController, contentPadding)
    }
}

@Composable
private fun BottomBar(
    state: MainNavigationState
) {
    val destinations = remember { MainNavigationDestinations.asList }
    BottomBarLayout(
        currentDestination = destinations.find {
            it.id == state.navController.currentBackStackEntryAsState().value?.destination?.route
        },
        destinations = destinations,
        navigate = { target ->
            val currentRoute = state.navController.currentDestination?.route
            if (currentRoute != target.id) {
                state.navController.navigate(target.id) {
                    val isCurrentStart =
                        state.navController.graph.findStartDestination().route == currentRoute
                    restoreState = true
                    launchSingleTop = true
                    popUpTo(currentRoute ?: "") {
                        inclusive = !isCurrentStart
                        saveState = true
                    }
                }
            }
        }
    )
}

@Composable
private fun BottomBarLayout(
    currentDestination: MainNavigationDestination?,
    destinations: List<MainNavigationDestination>,
    navigate: (MainNavigationDestination) -> Unit
) {
    Material3Surface(
        color = run {
            val tone = Material3Theme.primaryColorAsState().value
            val surface = Material3Theme.surfaceColorAsState().value
            val alpha = remember {
                val elevation = 5f
                ((4.5f * ln(x = elevation + 1)) + 2f) / 100f
            }
            remember(alpha, tone, surface) {
                tone
                    .copy(alpha = alpha)
                    .compositeOver(surface)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                for (destination in destinations) {
                    key(destination.id) {
                        val selected = currentDestination == destination
                        val interactionSource = remember { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        val sizeFactor by animateFloatAsState(targetValue = if (pressed) 0.94f else 1f)
                        NavigationBarItem(
                            selected,
                            onClick = {
                                if (destination == currentDestination) return@NavigationBarItem
                                navigate(destination)
                            },
                            icon = {
                                Box(modifier = Modifier.size(30.dp)) {
                                    val painter = painterResource(id = destination.resId)
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(27.dp * sizeFactor),
                                        painter = painter,
                                        tint = if (selected) {
                                            Material3Theme.secondaryContainerContentColorAsState().value
                                        } else {
                                            Material3Theme.surfaceVariantContentColorAsState().value
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
                            label = {
                                Text(
                                    color = Material3Theme.backgroundContentColorAsState().value,
                                    fontWeight = MaterialTheme.typography.labelMedium.fontWeight,
                                    fontSize = (MaterialTheme.typography.labelMedium.fontSize.value).sp,
                                    fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
                                    lineHeight = MaterialTheme.typography.labelMedium.lineHeight,
                                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing,
                                    text = destination.label
                                )
                            },
                            interactionSource = interactionSource,
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        with(LocalDensity.current) {
                            WindowInsets.navigationBars
                                .getBottom(this)
                                .toDp()
                        }
                    )
            )
        }
    }

}

@Composable
private fun NavigationHost(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = "career"
    ) {
        composable("career") {
            CareerMain()
        }
        composable("live") {
            LiveMain()
        }
        composable("account") {

        }
    }
}