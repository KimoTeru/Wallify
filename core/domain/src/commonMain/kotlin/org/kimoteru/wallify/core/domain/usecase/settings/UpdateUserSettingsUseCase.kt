package org.kimoteru.wallify.core.domain.usecase.settings

import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.domain.usecase.SuspendUseCase
import org.kimoteru.wallify.core.model.settings.UserSettings

class UpdateUserSettingsUseCase(
    private val repository: UserSettingsRepository,
    dispatchers: AppDispatchers
) : SuspendUseCase<UserSettings, Response<UserSettings>>(dispatchers) {
    override suspend fun execute(params: UserSettings): Response<UserSettings> =
        repository.update(params)
}