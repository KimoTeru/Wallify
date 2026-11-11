package org.kimoteru.wallify.feature.main.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassDock
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassDrawer
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassNavItem
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassRail
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationPadding
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalOverlayHost
import org.kimoteru.wallify.core.designsystem.ui.glass.NavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.OverlayHost
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.app_name
import org.kimoteru.wallify.core.resources.generated.resources.nav_feed
import org.kimoteru.wallify.core.resources.generated.resources.nav_random
import org.kimoteru.wallify.core.resources.generated.resources.nav_settings
import org.kimoteru.wallify.core.resources.generated.resources.nav_studio
import org.kimoteru.wallify.core.resources.generated.resources.nav_topics
import org.kimoteru.wallify.feature.main.presentation.components.MainGraphComponent
import org.kimoteru.wallify.feature.main.presentation.components.toTab
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract.UiEvent
import org.kimoteru.wallify.feature.main.presentation.ui.utils.MainScreens

object MainGraphContract {
    enum class Tab(
        val icon: ImageVector,
        val label: StringResource
    ) {
        Feed(AppIcons.GridView, Res.string.nav_feed),
        Topics(AppIcons.AutoAwesome, Res.string.nav_topics),
        Random(AppIcons.Shuffle, Res.string.nav_random),
        Studio(AppIcons.Camera, Res.string.nav_studio),
        Settings(AppIcons.Settings, Res.string.nav_settings)
    }

    @Immutable
    data class UiState(
        val tabs: List<Tab> = Tab.entries
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTab(val tab: Tab) : UiEvent
    }
}

/**
 * The main navigation of the signed-in user, which adapts to the window: a floating dock on phones, a floating
 * rail on tablets and a permanent drawer on large windows. The screens read the room it takes from
 * [LocalNavigationPadding], so their lists scroll under the dock and the rail.
 */
@Composable
fun MainGraphUi(
    modifier: Modifier = Modifier,
    component: MainGraphComponent
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val windowType = rememberWindowType()
    val layoutDirection = LocalLayoutDirection.current
    // In landscape the camera sits on the start edge, where the status bar was in portrait.
    val safeStart = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Start)
        .asPaddingValues()
        .calculateStartPadding(layoutDirection)
    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val stack by component.stack.subscribeAsState()

    val items = uiState.tabs.map { tab -> GlassNavItem(tab.icon, stringResource(tab.label)) }
    val selectedIndex = uiState.tabs.indexOf(stack.active.instance.toTab())

    val navigationVisibility = remember { NavigationVisibility() }
    val overlayHost = remember { OverlayHost() }

    // A screen that hid the bars while scrolling starts with them shown again after a switch of the tab.
    LaunchedEffect(selectedIndex) { navigationVisibility.isVisible = true }

    // The lists are the blur sources (`PhotoGridLayout`); the dock, the rail and the bars of the screens are their
    // siblings, so they frost what is under them. Sheets of the screens are drawn last, above the dock.
    CompositionLocalProvider(
        LocalNavigationVisibility provides navigationVisibility,
        LocalOverlayHost provides overlayHost
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            when (windowType) {
                WindowType.Compact -> Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalNavigationPadding provides PaddingValues(
                            bottom = appSize.navigation.dockHeight + appSize.navigation.dockInset
                        )
                    ) {
                        MainScreens(modifier = Modifier.fillMaxSize(), stack = component.stack)
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = navigationVisibility.isVisible,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        GlassDock(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(
                                    start = appPadding.medium,
                                    end = appPadding.medium,
                                    bottom = appSize.navigation.dockInset
                                ),
                            items = items,
                            selectedIndex = selectedIndex,
                            onSelect = { index ->
                                obtainEvent(UiEvent.SelectTab(uiState.tabs[index]))
                            }
                        )
                    }
                }

                WindowType.Medium -> Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalNavigationPadding provides PaddingValues(
                            start = safeStart + appSize.navigation.railWidth + appPadding.medium
                        )
                    ) {
                        MainScreens(modifier = Modifier.fillMaxSize(), stack = component.stack)
                    }

                    GlassRail(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                            .padding(start = appPadding.medium),
                        items = items,
                        selectedIndex = selectedIndex,
                        onSelect = { index -> obtainEvent(UiEvent.SelectTab(uiState.tabs[index])) }
                    )
                }

                WindowType.Expanded -> Row(modifier = Modifier.fillMaxSize()) {
                    GlassDrawer(
                        modifier = Modifier
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Start + WindowInsetsSides.Top + WindowInsetsSides.Bottom
                                )
                            )
                            .padding(appPadding.medium),
                        title = stringResource(Res.string.app_name),
                        items = items,
                        selectedIndex = selectedIndex,
                        onSelect = { index -> obtainEvent(UiEvent.SelectTab(uiState.tabs[index])) }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MainScreens(modifier = Modifier.fillMaxSize(), stack = component.stack)
                    }
                }
            }

            overlayHost.content?.invoke(this)
        }
    }
}