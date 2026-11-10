package org.kimoteru.wallify.feature.photo.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponentCore
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.ComponentEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiState
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultPhotoComponent(
    componentContext: ComponentContext,
    photoId: String,
    navigateBack: () -> Unit,
    private val openProfile: (username: String) -> Unit
) : BaseDefaultComponentCore<PhotoLogic, UiState, UiEvent, PhotoNavigationEvent, ComponentEvent>(
    componentContext = componentContext,
    navigateBack = navigateBack
),
    PhotoComponent {

    override val logic: PhotoLogic = instanceKeeper.getOrCreate { get<PhotoLogic> { parametersOf(photoId) } }

    override fun obtainNavigateEvent(event: PhotoNavigationEvent) {
        when (event) {
            is PhotoNavigationEvent.OpenProfile -> openProfile(event.username)
        }
    }
}