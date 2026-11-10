package org.kimoteru.wallify.feature.settings.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiEvent
import org.kimoteru.wallify.feature.settings.presentation.ui.SettingsContract.UiState
import org.koin.core.component.get

class DefaultSettingsComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<SettingsLogic, UiState, UiEvent, SettingsNavigationEvent>(
    componentContext = componentContext
),
    SettingsComponent {

    override val logic: SettingsLogic = instanceKeeper.getOrCreate { get<SettingsLogic>() }

    override fun obtainNavigateEvent(event: SettingsNavigationEvent) = Unit
}