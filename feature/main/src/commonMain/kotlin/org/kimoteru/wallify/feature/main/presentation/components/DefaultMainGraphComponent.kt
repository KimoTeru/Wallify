package org.kimoteru.wallify.feature.main.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.presentation.base.BaseNavigationDefaultComponentCore
import org.kimoteru.wallify.feature.feed.presentation.components.DefaultFeedComponent
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract.UiEvent
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphContract.UiState
import org.kimoteru.wallify.feature.profile.presentation.components.DefaultProfileComponent
import org.kimoteru.wallify.feature.profile.presentation.components.ProfileTarget
import org.kimoteru.wallify.feature.random.presentation.components.DefaultRandomComponent
import org.kimoteru.wallify.feature.settings.presentation.components.DefaultSettingsComponent
import org.kimoteru.wallify.feature.topics.presentation.components.DefaultTopicsComponent
import org.koin.core.component.get

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
        UiState,
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
                DefaultFeedComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Topics -> MainGraphComponent.Child.Topics(
                DefaultTopicsComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Random -> MainGraphComponent.Child.Random(
                DefaultRandomComponent(componentContext = componentContext, openPhoto = openPhoto)
            )

            is Config.Studio -> MainGraphComponent.Child.Studio(
                DefaultProfileComponent(
                    componentContext = componentContext,
                    target = ProfileTarget.CurrentUser,
                    navigateBack = null,
                    openPhoto = openPhoto
                )
            )

            is Config.Settings -> MainGraphComponent.Child.Settings(
                DefaultSettingsComponent(componentContext = componentContext)
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