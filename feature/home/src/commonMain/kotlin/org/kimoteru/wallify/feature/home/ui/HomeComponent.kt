package org.kimoteru.wallify.feature.home.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface HomeComponent : BaseComponent<HomeContract.UiState, HomeContract.UiEvent>

class HomeDefaultComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<HomeLogic, HomeContract.UiState, HomeContract.UiEvent, HomeNavigationEvent>(
    componentContext = componentContext
),
    HomeComponent {

    override val logic: HomeLogic = instanceKeeper.getOrCreate { get<HomeLogic>() }

    override fun obtainNavigateEvent(event: HomeNavigationEvent) = Unit
}

@Factory
class HomeLogic : BaseLogic<HomeContract.UiState, HomeContract.UiEvent, HomeNavigationEvent>() {

    override val uiState: StateFlow<HomeContract.UiState>
        field = MutableStateFlow(HomeContract.UiState())

    override fun obtainUiEvent(event: HomeContract.UiEvent) = Unit
}

sealed interface HomeNavigationEvent : BaseNavigationEvent