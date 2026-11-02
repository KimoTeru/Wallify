package org.kimoteru.wallify.feature.root.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.presentation.base.BaseNavigationDefaultComponentCore
import org.kimoteru.wallify.feature.home.ui.HomeDefaultComponent
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

    override fun obtainNavigateEvent(event: RootGraphNavigationEvent) {
        when (event) {
            is RootGraphNavigationEvent.Home -> navigateClearBackStack(Config.Home)
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
                    navigateNext = { obtainNavigateEvent(RootGraphNavigationEvent.Home) }
                )
            )

            is Config.Home -> RootGraphComponent.Child.Home(
                HomeDefaultComponent(componentContext = componentContext)
            )
        }
    }

    @Serializable
    sealed interface Config : BaseNavigationConfig {
        @Serializable
        data object Splash : Config

        @Serializable
        data object Home : Config
    }
}