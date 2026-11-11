package org.kimoteru.wallify.feature.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.ScreenOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberHideOnScrollConnection
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.paging.ListRefreshErrors
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.feature.profile.presentation.components.ProfileComponent
import org.kimoteru.wallify.feature.profile.presentation.ui.utils.ProfileEditOverlay
import org.kimoteru.wallify.feature.profile.presentation.ui.utils.ProfileGrid

object ProfileContract {
    enum class Tab { Photos, Collections }

    /** Fields of the profile form. */
    enum class EditField { Username, FirstName, LastName, Email, Portfolio, Location, Bio, Instagram }

    /** Text of the profile form: every field is a string, an empty one means "not set". */
    @Immutable
    data class EditForm(
        val username: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val portfolio: String,
        val location: String,
        val bio: String,
        val instagram: String
    ) {

        /** The backend refuses a profile without a username. */
        val isValid: Boolean
            get() = username.isNotBlank()

        fun with(field: EditField, value: String): EditForm = when (field) {
            EditField.Username -> copy(username = value)
            EditField.FirstName -> copy(firstName = value)
            EditField.LastName -> copy(lastName = value)
            EditField.Email -> copy(email = value)
            EditField.Portfolio -> copy(portfolio = value)
            EditField.Location -> copy(location = value)
            EditField.Bio -> copy(bio = value)
            EditField.Instagram -> copy(instagram = value)
        }

        /** Only the fields that differ from [user] are sent; the others stay as they are on the backend. */
        fun changesTo(user: User): UpdateUserParams = UpdateUserParams(
            username = changedOf(username, user.username),
            firstName = changedOf(firstName, user.firstName),
            lastName = changedOf(lastName, user.lastName),
            email = changedOf(email, user.email),
            portfolioUrl = changedOf(portfolio, user.portfolioUrl),
            location = changedOf(location, user.location),
            bio = changedOf(bio, user.bio),
            instagramUsername = changedOf(instagram, user.instagramUsername)
        )

        private fun changedOf(text: String, saved: String?): String? {
            val trimmed = text.trim()
            return trimmed.takeIf { it != saved.orEmpty() }
        }

        companion object {
            fun of(user: User): EditForm = EditForm(
                username = user.username,
                firstName = user.firstName.orEmpty(),
                lastName = user.lastName.orEmpty(),
                email = user.email.orEmpty(),
                portfolio = user.portfolioUrl.orEmpty(),
                location = user.location.orEmpty(),
                bio = user.bio.orEmpty(),
                instagram = user.instagramUsername.orEmpty()
            )
        }
    }

    @Immutable
    data class UiState(
        val user: User? = null,
        val statistics: Statistics? = null,
        val selectedTab: Tab = Tab.Photos,
        val openedCollection: PhotoCollection? = null,
        val isLoading: Boolean = true,
        /** Failure of the profile load; the screen picks its text by the type and shows the code. */
        val error: ResponseError? = null,

        /** The profile is the signed-in user's own: it can be edited and photos can be uploaded. */
        val isOwn: Boolean = false,

        /** The profile form while it is open, `null` while it is closed. */
        val editForm: EditForm? = null,
        val isSaving: Boolean = false,
        val listImageQuality: ImageQuality = UserSettings.Default.listImageQuality,
        /** Presses of «Повторить» on a failed refresh of the list of the tab ([UiEvent.ListRefreshError]). */
        val listRetries: Int = 0
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object Retry : UiEvent
        data class SelectTab(val tab: Tab) : UiEvent
        data class OpenCollection(val collection: PhotoCollection) : UiEvent
        data object CloseCollection : UiEvent
        data class OpenPhoto(val photoId: String) : UiEvent
        data object Upload : UiEvent
        data object ShowEdit : UiEvent
        data object HideEdit : UiEvent
        data class ChangeEditField(val field: EditField, val value: String) : UiEvent
        data object SaveEdit : UiEvent

        /** The refresh of a list on screen failed over shown items, or its failure is over (`null`). */
        data class ListRefreshError(val list: Long, val error: ResponseError?) : UiEvent
    }

    sealed interface ComponentEvent : BaseContract.ComponentEvent {
        data class OpenUrl(val url: String) : ComponentEvent
    }
}

@Preview
@Composable
fun PreviewProfileUi() {
    AppTheme {
        ProfileUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : ProfileComponent {
                override val canNavigateBack = false
                override val photos = flowOf(PagingData.empty<Photo>())
                override val collections = flowOf(PagingData.empty<PhotoCollection>())
                override val collectionPhotos = flowOf(PagingData.empty<Photo>())

                @Composable
                override fun getUiState(): ProfileContract.UiState = ProfileContract.UiState()
            }
        )
    }
}

@Composable
fun ProfileUi(
    modifier: Modifier = Modifier,
    component: ProfileComponent
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val screenPadding = rememberScreenPadding()
    val uriHandler = LocalUriHandler.current
    val isSheet = rememberWindowType() == WindowType.Compact

    component.collectComponentEvents { event ->
        when (event) {
            is ProfileContract.ComponentEvent.OpenUrl -> uriHandler.openUri(event.url)
        }
    }

    // A back button floats over the top left corner, so the content starts below it.
    val topPadding = if (component.canNavigateBack) {
        screenPadding.top + appSize.control.iconButton + appPadding.small
    } else {
        screenPadding.top
    }
    val contentPadding = PaddingValues(
        start = screenPadding.start,
        top = topPadding,
        end = screenPadding.end,
        bottom = screenPadding.bottom
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberHideOnScrollConnection(LocalNavigationVisibility.current))
    ) {
        RetryOnReconnect(
            isFailed = uiState.error != null && uiState.user == null,
            onRetry = { obtainEvent(ProfileContract.UiEvent.Retry) }
        )

        // Photos and collections of a profile are grids of equal cards whatever layout the feed uses.
        CompositionLocalProvider(LocalPhotoGridMode provides PhotoGridMode.Grid) {
            ListRefreshErrors(
                retries = uiState.listRetries,
                onChange = { list, error -> obtainEvent(ProfileContract.UiEvent.ListRefreshError(list, error)) }
            ) {
                ProfileGrid(
                    uiState = uiState,
                    photos = component.photos,
                    collections = component.collections,
                    collectionPhotos = component.collectionPhotos,
                    contentPadding = contentPadding,
                    obtainEvent = obtainEvent
                )
            }
        }

        if (component.canNavigateBack) {
            GlassIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(appPadding.medium),
                icon = AppIcons.Back,
                onClick = { component.obtainBaseEvent(BaseContract.UiEvent.NavigateBack) }
            )
        }

    }

    uiState.editForm?.let { form ->
        ScreenOverlay {
            ProfileEditOverlay(
                isSheet = isSheet,
                form = form,
                isSaving = uiState.isSaving,
                obtainEvent = obtainEvent
            )
        }
    }
}