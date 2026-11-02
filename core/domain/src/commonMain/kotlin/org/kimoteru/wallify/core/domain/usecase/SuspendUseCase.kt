package org.kimoteru.wallify.core.domain.usecase

import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers

abstract class SuspendUseCase<in P, out R>(
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(params: P): R = withContext(dispatchers.io) {
        execute(params)
    }

    protected abstract suspend fun execute(params: P): R
}

suspend operator fun <R> SuspendUseCase<Unit, R>.invoke(): R = invoke(Unit)