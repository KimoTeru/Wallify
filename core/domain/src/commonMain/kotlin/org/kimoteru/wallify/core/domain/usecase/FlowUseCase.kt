package org.kimoteru.wallify.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers

abstract class FlowUseCase<in P, out R>(
    private val dispatchers: AppDispatchers
) {
    operator fun invoke(params: P): Flow<R> = execute(params).flowOn(dispatchers.io)

    protected abstract fun execute(params: P): Flow<R>
}

operator fun <R> FlowUseCase<Unit, R>.invoke(): Flow<R> = invoke(Unit)