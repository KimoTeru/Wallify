package org.kimoteru.wallify.feature.settings.presentation.components

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.RequestStatsRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.domain.usecase.settings.UpdateUserSettingsUseCase
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.photo.handle
import org.kimoteru.wallify.core.presentation.photo.toAvatarUi
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.toDomain
import org.kimoteru.wallify.core.presentation.settings.toPhotoColumns
import org.kimoteru.wallify.core.presentation.settings.toPresentation
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.Account
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiEvent
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiState
import org.koin.core.annotation.Factory

interface SettingsComponent : BaseComponent<UiState, UiEvent>

/**
 * The account, the color scheme and the numbers of the app's requests to the photo service. The scheme is kept in
 * the user settings and the root graph applies it; signing out needs no navigation here: the root graph sees the
 * missing session.
 */
@Factory
class SettingsLogic(
    private val authRepository: AuthRepository,
    private val updateUserSettings: UpdateUserSettingsUseCase,
    private val requestStatsRepository: RequestStatsRepository,
    userRepository: UserRepository,
    observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogic<UiState, UiEvent, SettingsNavigationEvent>() {

    private val settings: StateFlow<UserSettings> = observeUserSettings()
        .stateIn(coroutineScope, SharingStarted.Eagerly, UserSettings.Default)

    override val uiState: StateFlow<UiState> = combineState(
        coroutineScope,
        userRepository.currentUser,
        settings,
        requestStatsRepository.stats,
        initialValue = UiState(account = userRepository.currentUser.value?.toAccount())
    ) { user, userSettings, requestStats ->
        val requestDays = requestStatsRepository.lastDays(requestStats, REQUEST_DAYS).map { it.count }
        val photoLayout = userSettings.photoLayout.toPresentation()

        UiState(
            account = user?.toAccount(),
            themeMode = userSettings.themeMode.toPresentation(),
            photoLayout = photoLayout,
            photoColumns = userSettings.photoColumns.toPhotoColumns(),
            isPhotoColumnsAvailable = photoLayout != PhotoLayoutUi.List,
            screenLayout = userSettings.screenLayout.toPresentation(),
            listImageQuality = userSettings.listImageQuality.toPresentation(),
            detailImageQuality = userSettings.detailImageQuality.toPresentation(),
            downloadImageQuality = userSettings.downloadImageQuality.toPresentation(),
            languageTag = userSettings.languageTag,
            requestTotal = requestStats.total,
            canResetRequestStats = requestStats.total > 0L,
            topEndpoints = requestStats.endpoints.take(TOP_ENDPOINTS),
            rateLimit = requestStats.rateLimit,
            requestDays = requestDays,
            requestDaysTotal = requestDays.sum(),
            requestDaysMax = requestDays.maxOrNull() ?: 0L
        )
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectTheme -> saveSettings { copy(themeMode = event.themeMode.toDomain()) }
            is UiEvent.SelectPhotoLayout -> saveSettings { copy(photoLayout = event.photoLayout.toDomain()) }
            is UiEvent.SelectPhotoColumns -> saveSettings { copy(photoColumns = event.columns.toDomain()) }
            is UiEvent.SelectScreenLayout -> saveSettings { copy(screenLayout = event.screenLayout.toDomain()) }
            is UiEvent.SelectListImageQuality -> saveSettings { copy(listImageQuality = event.quality.toDomain()) }
            is UiEvent.SelectDetailImageQuality -> saveSettings { copy(detailImageQuality = event.quality.toDomain()) }
            is UiEvent.SelectDownloadImageQuality -> saveSettings {
                copy(downloadImageQuality = event.quality.toDomain())
            }
            is UiEvent.SelectLanguage -> saveSettings { copy(languageTag = event.languageTag) }
            is UiEvent.SignOut -> signOut()
            is UiEvent.ResetRequestStats -> resetRequestStats()
        }
    }

    /** Every choice on the screen is one field of the saved settings: the rest of them stay as they are. */
    private fun saveSettings(change: UserSettings.() -> UserSettings) {
        coroutineScope.launch {
            updateUserSettings(settings.value.change())
        }
    }

    private fun signOut() {
        coroutineScope.launch {
            authRepository.signOut()
        }
    }

    private fun resetRequestStats() {
        coroutineScope.launch {
            handleResponseDefault(request = { requestStatsRepository.reset() })
        }
    }

    /** The account block: the email under the name, or the handle when the email is not shared. */
    private fun User.toAccount(): Account = Account(
        name = name,
        subtitle = email ?: handle(),
        avatar = toAvatarUi(imageUrl = profileImage?.medium)
    )

    private companion object {
        /** How many of the most requested endpoints the screen lists. */
        const val TOP_ENDPOINTS = 8

        /** How many local days, today included, the chart of the requests shows. */
        const val REQUEST_DAYS = 30
    }
}

sealed interface SettingsNavigationEvent : BaseNavigationEvent