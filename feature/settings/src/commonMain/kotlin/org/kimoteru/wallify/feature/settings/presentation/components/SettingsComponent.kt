package org.kimoteru.wallify.feature.settings.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.StatsRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.domain.usecase.settings.UpdateUserSettingsUseCase
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.settings.ImageQualityUi
import org.kimoteru.wallify.core.presentation.settings.PhotoColumnsUi
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ScreenLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.core.presentation.settings.toDomain
import org.kimoteru.wallify.core.presentation.settings.toPhotoColumns
import org.kimoteru.wallify.core.presentation.settings.toPresentation
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiEvent
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiState
import org.koin.core.annotation.Factory
interface SettingsComponent : BaseComponent<UiState, UiEvent>

/**
 * The account, the color scheme and the numbers of the photo service. The scheme is kept in the user settings and
 * the root graph applies it; signing out needs no navigation here: the root graph sees the missing session.
 */
@Factory
class SettingsLogic(
    private val authRepository: AuthRepository,
    private val statsRepository: StatsRepository,
    private val updateUserSettings: UpdateUserSettingsUseCase,
    userRepository: UserRepository,
    observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogic<UiState, UiEvent, SettingsNavigationEvent>() {

    private val settings: StateFlow<UserSettings> = observeUserSettings()
        .stateIn(coroutineScope, SharingStarted.Eagerly, UserSettings.Default)

    private val serviceStats = MutableStateFlow(ServiceStats())

    override val uiState: StateFlow<UiState> = combineState(
        coroutineScope,
        userRepository.currentUser,
        settings,
        serviceStats,
        initialValue = UiState(user = userRepository.currentUser.value)
    ) { user, userSettings, stats ->
        UiState(
            user = user,
            themeMode = userSettings.themeMode.toPresentation(),
            photoLayout = userSettings.photoLayout.toPresentation(),
            photoColumns = userSettings.photoColumns.toPhotoColumns(),
            screenLayout = userSettings.screenLayout.toPresentation(),
            listImageQuality = userSettings.listImageQuality.toPresentation(),
            detailImageQuality = userSettings.detailImageQuality.toPresentation(),
            downloadImageQuality = userSettings.downloadImageQuality.toPresentation(),
            languageTag = userSettings.languageTag,
            totalStats = stats.total,
            monthStats = stats.month
        )
    }

    override fun onCreate() {
        coroutineScope.launch {
            statsRepository.getTotalStatsFlow().collect { resource ->
                if (resource is Resource.Success) updateTotalStats(resource.data)
            }
        }

        coroutineScope.launch {
            statsRepository.getMonthStatsFlow().collect { resource ->
                if (resource is Resource.Success) updateMonthStats(resource.data)
            }
        }
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectTheme -> selectTheme(event.themeMode)
            is UiEvent.SelectPhotoLayout -> selectPhotoLayout(event.photoLayout)
            is UiEvent.SelectPhotoColumns -> selectPhotoColumns(event.columns)
            is UiEvent.SelectScreenLayout -> selectScreenLayout(event.screenLayout)
            is UiEvent.SelectListImageQuality -> selectListImageQuality(event.quality)
            is UiEvent.SelectDetailImageQuality -> selectDetailImageQuality(event.quality)
            is UiEvent.SelectDownloadImageQuality -> selectDownloadImageQuality(event.quality)
            is UiEvent.SelectLanguage -> selectLanguage(event.languageTag)
            is UiEvent.SignOut -> coroutineScope.launch { authRepository.signOut() }
        }
    }

    private fun selectTheme(themeMode: ThemeModeUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(themeMode = themeMode.toDomain()))
        }
    }

    private fun selectPhotoLayout(photoLayout: PhotoLayoutUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(photoLayout = photoLayout.toDomain()))
        }
    }

    private fun selectPhotoColumns(columns: PhotoColumnsUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(photoColumns = columns.toDomain()))
        }
    }

    private fun selectScreenLayout(screenLayout: ScreenLayoutUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(screenLayout = screenLayout.toDomain()))
        }
    }

    private fun selectListImageQuality(quality: ImageQualityUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(listImageQuality = quality.toDomain()))
        }
    }

    private fun selectDetailImageQuality(quality: ImageQualityUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(detailImageQuality = quality.toDomain()))
        }
    }

    private fun selectDownloadImageQuality(quality: ImageQualityUi) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(downloadImageQuality = quality.toDomain()))
        }
    }

    private fun selectLanguage(languageTag: String) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(languageTag = languageTag))
        }
    }

    private fun updateTotalStats(total: TotalStats) {
        serviceStats.update { it.copy(total = total) }
    }

    private fun updateMonthStats(month: MonthStats) {
        serviceStats.update { it.copy(month = month) }
    }

    private data class ServiceStats(
        val total: TotalStats? = null,
        val month: MonthStats? = null
    )
}

sealed interface SettingsNavigationEvent : BaseNavigationEvent