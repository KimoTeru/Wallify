package org.kimoteru.wallify.feature.feed.presentation.components

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.UiEvent
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.UiState
import org.koin.core.component.get

class DefaultFeedComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<FeedLogic, UiState, UiEvent, FeedNavigationEvent>(
    componentContext = componentContext
),
    FeedComponent {

    override val logic: FeedLogic = retainedLogic { get<FeedLogic>() }

    override val photos: Flow<PagingData<Photo>> = logic.photos

    override fun obtainNavigateEvent(event: FeedNavigationEvent) {
        when (event) {
            is FeedNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}