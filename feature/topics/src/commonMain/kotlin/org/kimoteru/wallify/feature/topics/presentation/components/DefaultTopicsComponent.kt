package org.kimoteru.wallify.feature.topics.presentation.components

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiEvent
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiState
import org.koin.core.component.get

class DefaultTopicsComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<TopicsLogic, UiState, UiEvent, TopicsNavigationEvent>(
    componentContext = componentContext
),
    TopicsComponent {

    override val logic: TopicsLogic = retainedLogic { get<TopicsLogic>() }

    override val topics: Flow<PagingData<Topic>> = logic.topics

    override val photos: Flow<PagingData<PhotoCardUi>> = logic.photos

    override fun obtainNavigateEvent(event: TopicsNavigationEvent) {
        when (event) {
            is TopicsNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}