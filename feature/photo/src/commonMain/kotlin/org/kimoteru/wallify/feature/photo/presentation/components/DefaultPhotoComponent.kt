package org.kimoteru.wallify.feature.photo.presentation.components

import com.arkivanov.decompose.ComponentContext
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiState
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultPhotoComponent(
    componentContext: ComponentContext,
    photoId: String,
    navigateBack: () -> Unit,
    private val openProfile: (username: String) -> Unit
) : BaseDefaultComponent<PhotoLogic, UiState, UiEvent, PhotoNavigationEvent>(
    componentContext = componentContext,
    navigateBack = navigateBack
),
    PhotoComponent {

    override val logic: PhotoLogic = retainedLogic { get<PhotoLogic> { parametersOf(photoId) } }

    override fun obtainNavigateEvent(event: PhotoNavigationEvent) {
        when (event) {
            is PhotoNavigationEvent.OpenProfile -> openProfile(event.username)
        }
    }
}