package org.kimoteru.wallify.feature.main.presentation.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedUi
import org.kimoteru.wallify.feature.main.presentation.components.MainGraphComponent.Child
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileUi
import org.kimoteru.wallify.feature.random.presentation.ui.RandomUi
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsUi
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsUi

/** The screen of the selected tab. */
@Composable
internal fun MainScreens(
    modifier: Modifier = Modifier,
    stack: Value<ChildStack<*, Child>>
) {
    Children(
        stack = stack,
        modifier = modifier,
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