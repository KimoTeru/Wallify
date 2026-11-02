package org.kimoteru.wallify.core.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.domain.usecase.FlowUseCase
import org.kimoteru.wallify.core.model.settings.UserSettings

class ObserveUserSettingsUseCase(
    private val repository: UserSettingsRepository,
    dispatchers: AppDispatchers
) : FlowUseCase<Unit, UserSettings>(dispatchers) {
    override fun execute(params: Unit): Flow<UserSettings> = repository.observe()
}