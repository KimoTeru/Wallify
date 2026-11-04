package org.kimoteru.wallify.feature.settings.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
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
import org.kimoteru.wallify.core.model.settings.PhotoLayout
import org.kimoteru.wallify.core.model.settings.ScreenLayout
import org.kimoteru.wallify.core.model.settings.ThemeMode
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.settings.ui.SettingsContract.UiEvent
import org.kimoteru.wallify.feature.settings.ui.SettingsContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface SettingsComponent : BaseComponent<UiState, UiEvent>

class SettingsDefaultComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<SettingsLogic, UiState, UiEvent, SettingsNavigationEvent>(
    componentContext = componentContext
),
    SettingsComponent {

    override val logic: SettingsLogic = instanceKeeper.getOrCreate { get<SettingsLogic>() }

    override fun obtainNavigateEvent(event: SettingsNavigationEvent) = Unit
}

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
            themeMode = userSettings.themeMode,
            photoLayout = userSettings.photoLayout,
            photoColumns = userSettings.photoColumns,
            screenLayout = userSettings.screenLayout,
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
            is UiEvent.SelectLanguage -> selectLanguage(event.languageTag)
            is UiEvent.SignOut -> coroutineScope.launch { authRepository.signOut() }
        }
    }

    private fun selectTheme(themeMode: ThemeMode) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(themeMode = themeMode))
        }
    }

    private fun selectPhotoLayout(photoLayout: PhotoLayout) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(photoLayout = photoLayout))
        }
    }

    private fun selectPhotoColumns(columns: Int?) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(photoColumns = columns))
        }
    }

    private fun selectScreenLayout(screenLayout: ScreenLayout) {
        coroutineScope.launch {
            updateUserSettings(settings.value.copy(screenLayout = screenLayout))
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