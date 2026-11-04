package org.kimoteru.wallify.feature.auth.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.feature.auth.presentation.ui.AuthContract
import org.koin.core.component.get

class DefaultAuthComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<AuthLogic, AuthContract.UiState, AuthContract.UiEvent, AuthNavigationEvent>(
    componentContext = componentContext
),
    AuthComponent {

    override val logic: AuthLogic = instanceKeeper.getOrCreate { get<AuthLogic>() }

    override fun obtainNavigateEvent(event: AuthNavigationEvent) = Unit
}