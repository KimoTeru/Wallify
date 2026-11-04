package org.kimoteru.wallify.feature.root.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
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
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.app_name
import org.kimoteru.wallify.core.resources.generated.resources.nav_feed
import org.kimoteru.wallify.core.resources.generated.resources.nav_random
import org.kimoteru.wallify.core.resources.generated.resources.nav_settings
import org.kimoteru.wallify.core.resources.generated.resources.nav_studio
import org.kimoteru.wallify.core.resources.generated.resources.nav_topics
import org.kimoteru.wallify.feature.feed.ui.FeedUi
import org.kimoteru.wallify.feature.profile.ui.ProfileUi
import org.kimoteru.wallify.feature.random.ui.RandomUi
import org.kimoteru.wallify.feature.root.main.MainGraphComponent.Child
import org.kimoteru.wallify.feature.settings.ui.SettingsUi
import org.kimoteru.wallify.feature.topics.ui.TopicsUi

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
    val stack by component.stack.subscribeAsState()

    val items = listOf(
        GlassNavItem(AppIcons.GridView, stringResource(Res.string.nav_feed)),
        GlassNavItem(AppIcons.AutoAwesome, stringResource(Res.string.nav_topics)),
        GlassNavItem(AppIcons.Shuffle, stringResource(Res.string.nav_random)),
        GlassNavItem(AppIcons.Camera, stringResource(Res.string.nav_studio)),
        GlassNavItem(AppIcons.Settings, stringResource(Res.string.nav_settings))
    )

    val selectedIndex = when (stack.active.instance) {
        is Child.Feed -> MainGraphContract.Tab.Feed.ordinal
        is Child.Topics -> MainGraphContract.Tab.Topics.ordinal
        is Child.Random -> MainGraphContract.Tab.Random.ordinal
        is Child.Studio -> MainGraphContract.Tab.Studio.ordinal
        is Child.Settings -> MainGraphContract.Tab.Settings.ordinal
    }

    val onSelect: (Int) -> Unit = { index ->
        component.obtainUiEvent(MainGraphContract.UiEvent.SelectTab(MainGraphContract.Tab.entries[index]))
    }

    val screens: @Composable (Modifier) -> Unit = { screenModifier ->
        Children(
            stack = component.stack,
            modifier = screenModifier,
            animation = stackAnimation(fade())
        ) { child ->
            when (val instance = child.instance) {
                is Child.Feed -> FeedUi(component = instance.component)
                is Child.Topics -> TopicsUi(component = instance.component)
                is Child.Random -> RandomUi(component = instance.component)
                is Child.Studio -> ProfileUi(component = instance.component)
                is Child.Settings -> SettingsUi(component = instance.component)
            }
        }
    }

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
                        screens(Modifier.fillMaxSize())
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
                                .padding(bottom = appSize.navigation.dockInset),
                            items = items,
                            selectedIndex = selectedIndex,
                            onSelect = onSelect
                        )
                    }
                }

                WindowType.Medium -> Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalNavigationPadding provides PaddingValues(
                            start = appSize.navigation.railWidth + appSize.navigation.dockInset
                        )
                    ) {
                        screens(Modifier.fillMaxSize())
                    }

                    GlassRail(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = appSize.navigation.dockInset),
                        items = items,
                        selectedIndex = selectedIndex,
                        onSelect = onSelect
                    )
                }

                WindowType.Expanded -> Row(modifier = Modifier.fillMaxSize()) {
                    GlassDrawer(
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(appPadding.medium),
                        title = stringResource(Res.string.app_name),
                        items = items,
                        selectedIndex = selectedIndex,
                        onSelect = onSelect
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        screens(Modifier.fillMaxSize())
                    }
                }
            }

            overlayHost.content?.invoke(this)
        }
    }
}