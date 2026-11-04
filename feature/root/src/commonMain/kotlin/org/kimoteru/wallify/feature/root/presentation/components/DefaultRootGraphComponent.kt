package org.kimoteru.wallify.feature.root.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.presentation.base.BaseNavigationDefaultComponentCore
import org.kimoteru.wallify.feature.auth.ui.AuthDefaultComponent
import org.kimoteru.wallify.feature.photo.ui.PhotoDefaultComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileDefaultComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileTarget
import org.kimoteru.wallify.feature.root.main.DefaultMainGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract
import org.kimoteru.wallify.feature.splash.ui.SplashDefaultComponent
import org.koin.core.component.get

class DefaultRootGraphComponent(
    componentContext: ComponentContext,
    private val finishApp: () -> Unit,
    initialConfiguration: Config = Config.Splash
) : BaseNavigationDefaultComponentCore<
        RootGraphComponent.Child,
        DefaultRootGraphComponent.Config,
        RootGraphLogic,
        RootGraphContract.UiState,
        RootGraphContract.UiEvent,
        RootGraphNavigationEvent,
        Nothing
        >(
    componentContext = componentContext,
    finishApp = finishApp
),
    RootGraphComponent {

    override val stack: Value<ChildStack<*, RootGraphComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = initialConfiguration,
            handleBackButton = true,
            childFactory = ::child
        )

    override val logic: RootGraphLogic = instanceKeeper.getOrCreate { get<RootGraphLogic>() }

    init {
        // The app restored past the splash has no splash to wait for: the session decides the screen at once.
        if (stack.value.active.configuration !is Config.Splash) {
            logic.onSplashFinished()
        }
    }

    override fun obtainNavigateEvent(event: RootGraphNavigationEvent) {
        when (event) {
            is RootGraphNavigationEvent.Main -> navigateClearBackStack(Config.Main)
            is RootGraphNavigationEvent.Auth -> navigateClearBackStack(Config.Auth)
        }
    }

    override fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootGraphComponent.Child {
        return when (config) {
            is Config.Splash -> RootGraphComponent.Child.Splash(
                SplashDefaultComponent(
                    componentContext = componentContext,
                    navigateNext = { logic.onSplashFinished() }
                )
            )

            is Config.Main -> RootGraphComponent.Child.Main(
                DefaultMainGraphComponent(
                    componentContext = componentContext,
                    openPhoto = { photoId -> navigate(Config.Photo(photoId)) }
                )
            )

            is Config.Photo -> RootGraphComponent.Child.Photo(
                PhotoDefaultComponent(
                    componentContext = componentContext,
                    photoId = config.photoId,
                    navigateBack = { onBackClicked() },
                    openProfile = { username -> navigate(Config.Profile(username)) }
                )
            )

            is Config.Profile -> RootGraphComponent.Child.Profile(
                ProfileDefaultComponent(
                    componentContext = componentContext,
                    target = ProfileTarget.Username(config.username),
                    navigateBack = { onBackClicked() },
                    openPhoto = { photoId -> navigate(Config.Photo(photoId)) }
                )
            )

            is Config.Auth -> RootGraphComponent.Child.Auth(
                AuthDefaultComponent(componentContext = componentContext)
            )
        }
    }

    @Serializable
    sealed interface Config : BaseNavigationConfig {
        @Serializable
        data object Splash : Config

        @Serializable
        data object Main : Config

        @Serializable
        data class Photo(val photoId: String) : Config

        @Serializable
        data class Profile(val username: String) : Config

        @Serializable
        data object Auth : Config
    }
}