package org.kimoteru.wallify.feature.random.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.random.presentation.ui.RandomContract.UiEvent
import org.kimoteru.wallify.feature.random.presentation.ui.RandomContract.UiState
import org.koin.core.component.get

class DefaultRandomComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<RandomLogic, UiState, UiEvent, RandomNavigationEvent>(
    componentContext = componentContext
),
    RandomComponent {

    override val logic: RandomLogic = instanceKeeper.getOrCreate { get<RandomLogic>() }

    override fun obtainNavigateEvent(event: RandomNavigationEvent) {
        when (event) {
            is RandomNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}