package org.kimoteru.wallify.feature.root.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.base.BaseNavigationComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationDefaultComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.base.BaseNavigationLogicCore
import org.kimoteru.wallify.feature.feed.ui.FeedComponent
import org.kimoteru.wallify.feature.feed.ui.FeedDefaultComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileDefaultComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileTarget
import org.kimoteru.wallify.feature.random.ui.RandomComponent
import org.kimoteru.wallify.feature.random.ui.RandomDefaultComponent
import org.kimoteru.wallify.feature.root.main.MainGraphContract.UiEvent
import org.kimoteru.wallify.feature.settings.ui.SettingsComponent
import org.kimoteru.wallify.feature.settings.ui.SettingsDefaultComponent
import org.kimoteru.wallify.feature.topics.ui.TopicsComponent
import org.kimoteru.wallify.feature.topics.ui.TopicsDefaultComponent
import org.koin.core.annotation.Factory
import org.koin.core.component.get

object MainGraphContract {
    enum class Tab { Feed, Topics, Random, Studio, Settings }

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTab(val tab: Tab) : UiEvent
    }
}

interface MainGraphComponent : BaseNavigationComponentCore<Nothing, UiEvent, MainGraphComponent.Child, Nothing> {
    sealed interface Child : BaseNavigationComponentCore.BaseNavigationChild {
        class Feed(val component: FeedComponent) : Child
        class Topics(val component: TopicsComponent) : Child
        class Random(val component: RandomComponent) : Child
        class Studio(val component: ProfileComponent) : Child
        class Settings(val component: SettingsComponent) : Child
    }
}

/**
 * The screens of the signed-in user behind one navigation: feed, curated topics, random photos, the user's own
 * profile and the settings. The tabs keep their state while the user switches between them; opening a photo is
 * left to the root graph, which puts it over the whole navigation.
 */
class DefaultMainGraphComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseNavigationDefaultComponentCore<
        MainGraphComponent.Child,
        DefaultMainGraphComponent.Config,
        MainGraphLogic,
        Nothing,
        UiEvent,
        MainGraphNavigationEvent,
        Nothing
        >(
    componentContext = componentContext
),
    MainGraphComponent {

    override val stack: Value<ChildStack<*, MainGraphComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Feed,
            handleBackButton = true,
            childFactory = ::child
        )

    override val logic: MainGraphLogic = instanceKeeper.getOrCreate { get<MainGraphLogic>() }

    override fun obtainNavigateEvent(event: MainGraphNavigationEvent) {
        when (event) {
            is MainGraphNavigationEvent.SelectTab -> navigation.bringToFront(event.tab.toConfig())
        }
    }

    override fun child(
        config: Config,
        componentContext: ComponentContext
    ): MainGraphComponent.Child {
        return when (config) {
            is Config.Feed -> MainGraphComponent.Child.Feed(
                FeedDefaultComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Topics -> MainGraphComponent.Child.Topics(
                TopicsDefaultComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Random -> MainGraphComponent.Child.Random(
                RandomDefaultComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Studio -> MainGraphComponent.Child.Studio(
                ProfileDefaultComponent(
                    componentContext = componentContext,
                    target = ProfileTarget.CurrentUser,
                    navigateBack = null,
                    openPhoto = openPhoto
                )
            )

            is Config.Settings -> MainGraphComponent.Child.Settings(
                SettingsDefaultComponent(componentContext = componentContext)
            )
        }
    }

    private fun MainGraphContract.Tab.toConfig(): Config = when (this) {
        MainGraphContract.Tab.Feed -> Config.Feed
        MainGraphContract.Tab.Topics -> Config.Topics
        MainGraphContract.Tab.Random -> Config.Random
        MainGraphContract.Tab.Studio -> Config.Studio
        MainGraphContract.Tab.Settings -> Config.Settings
    }

    @Serializable
    sealed interface Config : BaseNavigationConfig {
        @Serializable
        data object Feed : Config

        @Serializable
        data object Topics : Config

        @Serializable
        data object Random : Config

        @Serializable
        data object Studio : Config

        @Serializable
        data object Settings : Config
    }
}

@Factory
class MainGraphLogic : BaseNavigationLogicCore<Nothing, UiEvent, MainGraphNavigationEvent, Nothing>() {

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectTab -> navigate(MainGraphNavigationEvent.SelectTab(event.tab))
        }
    }
}

sealed interface MainGraphNavigationEvent : BaseNavigationEvent {
    data class SelectTab(val tab: MainGraphContract.Tab) : MainGraphNavigationEvent
}