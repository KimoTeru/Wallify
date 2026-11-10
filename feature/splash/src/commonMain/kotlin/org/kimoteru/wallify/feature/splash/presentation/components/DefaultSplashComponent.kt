package org.kimoteru.wallify.feature.splash.presentation.components

import com.arkivanov.decompose.ComponentContext
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashContract
import org.koin.core.component.get

class DefaultSplashComponent(
    componentContext: ComponentContext,
    private val navigateNext: () -> Unit
) : BaseDefaultComponent<SplashLogic, SplashContract.UiState, SplashContract.UiEvent, SplashNavigationEvent>(
    componentContext = componentContext
),
    SplashComponent {

    override val logic: SplashLogic = retainedLogic { get<SplashLogic>() }

    override fun obtainNavigateEvent(event: SplashNavigationEvent) {
        when (event) {
            is SplashNavigationEvent.Next -> navigateNext()
        }
    }
}