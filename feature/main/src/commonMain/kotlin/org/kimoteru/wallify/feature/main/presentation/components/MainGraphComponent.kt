package org.kimoteru.wallify.feature.main.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.presentation.base.BaseNavigationComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.base.BaseNavigationLogicCore
import org.kimoteru.wallify.feature.feed.presentation.components.FeedComponent
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract.UiEvent
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract.UiState
import org.kimoteru.wallify.feature.profile.presentation.components.ProfileComponent
import org.kimoteru.wallify.feature.random.presentation.components.RandomComponent
import org.kimoteru.wallify.feature.settings.presentation.components.SettingsComponent
import org.kimoteru.wallify.feature.topics.presentation.components.TopicsComponent
import org.koin.core.annotation.Factory

interface MainGraphComponent : BaseNavigationComponentCore<UiState, UiEvent, MainGraphComponent.Child, Nothing> {
    sealed interface Child : BaseNavigationComponentCore.BaseNavigationChild {
        class Feed(val component: FeedComponent) : Child
        class Topics(val component: TopicsComponent) : Child
        class Random(val component: RandomComponent) : Child
        class Studio(val component: ProfileComponent) : Child
        class Settings(val component: SettingsComponent) : Child
    }
}

fun MainGraphComponent.Child.toTab(): MainGraphContract.Tab = when (this) {
    is MainGraphComponent.Child.Feed -> MainGraphContract.Tab.Feed
    is MainGraphComponent.Child.Topics -> MainGraphContract.Tab.Topics
    is MainGraphComponent.Child.Random -> MainGraphContract.Tab.Random
    is MainGraphComponent.Child.Studio -> MainGraphContract.Tab.Studio
    is MainGraphComponent.Child.Settings -> MainGraphContract.Tab.Settings
}

@Factory
class MainGraphLogic : BaseNavigationLogicCore<UiState, UiEvent, MainGraphNavigationEvent, Nothing>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectTab -> navigate(MainGraphNavigationEvent.SelectTab(event.tab))
        }
    }
}

sealed interface MainGraphNavigationEvent : BaseNavigationEvent {
    data class SelectTab(val tab: MainGraphContract.Tab) : MainGraphNavigationEvent
}