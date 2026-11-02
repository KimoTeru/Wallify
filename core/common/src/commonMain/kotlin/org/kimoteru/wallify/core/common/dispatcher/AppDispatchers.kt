package org.kimoteru.wallify.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Injectable dispatchers, so code stays testable. */
interface AppDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class AppDispatchersImpl : AppDispatchers {
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}