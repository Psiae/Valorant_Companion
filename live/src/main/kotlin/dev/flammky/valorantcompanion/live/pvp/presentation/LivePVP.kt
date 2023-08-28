package dev.flammky.valorantcompanion.live.pvp.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import dev.flammky.valorantcompanion.base.compose.CompositionObserver
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.live.BuildConfig
import dev.flammky.valorantcompanion.live.main.LiveMainScreenScope
import dev.flammky.valorantcompanion.live.pvp.match.presentation.root.LiveMatchUI
import dev.flammky.valorantcompanion.live.pvp.party.presentation.LivePartyUI
import dev.flammky.valorantcompanion.live.pvp.ingame.presentation.LiveInGame
import dev.flammky.valorantcompanion.live.pvp.pregame.presentation.root.LivePreGameScreen

@Composable
fun LivePVP(
    modifier: Modifier,
    isVisibleToUser: Boolean,
    openScreen: (@Composable LiveMainScreenScope.() -> Unit) -> Unit
) {

    val preGameScreen = livePreGameScreen()
    val inGameScreen = liveInGameScreen()

    val screenHost = remember(openScreen) {
        var attachedToHost by mutableStateOf(false)
        var attachingHost = false
        var detachingHost = false
        val contents = mutableStateListOf<String>()
        var remembered = false
        var abandoned = false
        var forgotten = false

        object : RememberObserver {

            val hasContent: Boolean get() = contents.isNotEmpty()

            fun showPreGameDetail() {
                prepareAttachHost()
                contents.remove("pregame")
                contents.add("pregame")
            }

            fun showInGameDetail() {
                prepareAttachHost()
                contents.remove("ingame")
                contents.add("ingame")
            }

            override fun onAbandoned() {
                abandoned = true
                contents.clear()
            }

            override fun onForgotten() {
                forgotten = true
                contents.clear()
            }

            override fun onRemembered() {
                remembered = true
                contents.clear()
            }

            private fun LiveMainScreenScope.onContentRemoved() {
                if (contents.isEmpty()) this.dismiss()
                detachingHost = true
            }

            private fun prepareAttachHost() {
                if (!attachedToHost && !attachingHost) attachHost()
            }

            private fun attachHost() {
                detachingHost = false
                attachingHost = true
                openScreen.invoke {
                    if (attachedToHost) contents.forEach { content ->
                        when (content) {
                            "pregame" -> remember {
                                object : LivePvpScreenScope {
                                    override val hasFocus: Boolean
                                        get() = contents.lastOrNull() == "pregame"

                                    override fun dismiss() {
                                        contents.remove("pregame")
                                        onContentRemoved()
                                    }
                                }
                            }.preGameScreen()
                            "ingame" -> remember {
                                object : LivePvpScreenScope {
                                    override val hasFocus: Boolean
                                        get() = contents.lastOrNull() == "ingame"

                                    override fun dismiss() {
                                        contents.remove("ingame")
                                        onContentRemoved()
                                    }
                                }
                            }.inGameScreen()
                        }
                    }
                    CompositionObserver(
                        onRemembered = {
                            Log.d(
                                BuildConfig.LIBRARY_PACKAGE_NAME,
                                "pvp.presentation.LivePVPKt_LivePVP_screenHost_attachHost: onRemembered"
                            )
                            check(attachingHost) ; check(!detachingHost)
                            attachingHost = false ;  attachedToHost = true
                        },
                        onForgotten = {
                            Log.d(
                                BuildConfig.LIBRARY_PACKAGE_NAME,
                                "pvp.presentation.LivePVPKt_LivePVP_screenHost_attachHost: onForgotten"
                            )
                            check(attachedToHost)
                            detachingHost = false ; attachedToHost = false
                        },
                        onAbandoned = {
                            Log.d(
                                BuildConfig.LIBRARY_PACKAGE_NAME,
                                "pvp.presentation.LivePVPKt_LivePVP_screenHost_attachHost: onAbandoned"
                            )
                            check(attachingHost) ; check(!attachedToHost)
                            attachingHost = false
                        }
                    )
                }
            }
        }
    }

    LiveMainPlacement(
        modifier = modifier,
        liveParty = {
            LivePartyUI(
                modifier = Modifier,
                visibleToUser = isVisibleToUser && !screenHost.hasContent
            )
        },
        liveMatch = {
            LiveMatchUI(
                modifier = Modifier,
                visibleToUser = isVisibleToUser && !screenHost.hasContent,
                openPreGameDetail = screenHost::showPreGameDetail,
                openInGameDetail = screenHost::showInGameDetail
            )
        },
    )
}

private inline fun livePreGameScreen(): @Composable LivePvpScreenScope.() -> Unit {
    val fn: @Composable LivePvpScreenScope.() -> Unit = {
        LivePreGameScreen(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            dismiss = ::dismiss
        )
    }
    return fn
}

private inline fun liveInGameScreen(): @Composable LivePvpScreenScope.() -> Unit {
    val fn: @Composable LivePvpScreenScope.() -> Unit = {
        LiveInGame(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            dismiss = ::dismiss
        )
    }
    return fn
}

@Composable
private fun LiveMainPlacement(
    modifier: Modifier,
    liveParty: @Composable () -> Unit,
    liveMatch: @Composable () -> Unit,
) = Column(
    modifier = modifier
        .localMaterial3Background()
        .pointerInput(Unit) {}
        .statusBarsPadding()
) {
    liveParty()
    liveMatch()
}