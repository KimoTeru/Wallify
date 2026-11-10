package org.kimoteru.wallify.feature.profile.presentation.components

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponentCore
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.ComponentEvent
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.UiEvent
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.UiState
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultProfileComponent(
    componentContext: ComponentContext,
    target: ProfileTarget,
    navigateBack: (() -> Unit)?,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponentCore<ProfileLogic, UiState, UiEvent, ProfileNavigationEvent, ComponentEvent>(
    componentContext = componentContext,
    navigateBack = navigateBack
),
    ProfileComponent {

    override val logic: ProfileLogic = retainedLogic { get<ProfileLogic> { parametersOf(target) } }

    override val canNavigateBack: Boolean = navigateBack != null

    override val photos: Flow<PagingData<Photo>> = logic.photos

    override val collections: Flow<PagingData<PhotoCollection>> = logic.collections

    override val collectionPhotos: Flow<PagingData<Photo>> = logic.collectionPhotos

    override fun obtainNavigateEvent(event: ProfileNavigationEvent) {
        when (event) {
            is ProfileNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}