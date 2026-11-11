package org.kimoteru.wallify.feature.main.presentation.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedUi
import org.kimoteru.wallify.feature.main.presentation.components.MainGraphComponent.Child
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileUi
import org.kimoteru.wallify.feature.random.presentation.ui.RandomUi
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsUi
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsUi

/**
 * The screen of the selected tab, switched at once, without a transition. Every tab marks its content as the blur
 * source of the dock, and Haze blurs a source without the alpha of the layers around it: during a fade both tabs
 * would be under the dock at full opacity, and its blur would show the old tab until the fade ends.
 */
@Composable
internal fun MainScreens(
    modifier: Modifier = Modifier,
    stack: Value<ChildStack<*, Child>>
) {
    Children(
        stack = stack,
        modifier = modifier
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