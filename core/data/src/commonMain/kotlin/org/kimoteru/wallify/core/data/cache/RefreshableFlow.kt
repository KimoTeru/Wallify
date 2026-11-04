package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError

/**
 * One item with the database as the single source of truth: the screen only ever sees what the database holds.
 *
 * The saved row is emitted at once ([Resource.Success]) or, if there is none yet, [Resource.Loading]. In parallel
 * [remoteCall] asks the backend and [save] writes the answer into the database, which makes [roomFlow] emit the new
 * row. Every later change of the row (a refresh, a write from another screen) is emitted the same way.
 *
 * A failing backend does not touch what is shown: with a saved row the screen keeps it (offline works), without one
 * it gets [Resource.Error] and retries by collecting the flow again.
 */
internal fun <Entity : Any, Domain : Any, Remote : Any> refreshableFlow(
    roomFlow: () -> Flow<Entity?>,
    remoteCall: suspend () -> Response<Remote>,
    save: suspend (Remote) -> Unit,
    toDomain: (Entity) -> Domain
): Flow<Resource<Domain>> = channelFlow {
    var isFirst = true

    roomFlow().distinctUntilChanged().collect { entity ->
        if (isFirst) {
            isFirst = false
            send(entity?.let { Resource.Success(toDomain(it)) } ?: Resource.Loading)
            launch { refresh(hasSaved = entity != null, remoteCall = remoteCall, save = save) }
        } else if (entity != null) {
            send(Resource.Success(toDomain(entity)))
        }
    }
}

/**
 * A short list with the database as the single source of truth, the same way as [refreshableFlow]. An empty saved
 * list counts as "nothing yet" until the backend has answered; an empty answer is then emitted as an empty list.
 */
internal fun <Entity : Any, Domain : Any, Remote : Any> refreshableListFlow(
    roomFlow: () -> Flow<List<Entity>>,
    remoteCall: suspend () -> Response<List<Remote>>,
    save: suspend (List<Remote>) -> Unit,
    toDomain: (Entity) -> Domain
): Flow<Resource<List<Domain>>> = channelFlow {
    var isFirst = true

    roomFlow().distinctUntilChanged().collect { entities ->
        if (isFirst) {
            isFirst = false
            send(if (entities.isEmpty()) Resource.Loading else Resource.Success(entities.map(toDomain)))
            launch {
                refresh(
                    hasSaved = entities.isNotEmpty(),
                    remoteCall = remoteCall,
                    save = { remote ->
                        save(remote)
                        // Saving an empty list changes no row, so the database stays silent: tell the screen.
                        if (remote.isEmpty() && entities.isEmpty()) send(Resource.Success(emptyList()))
                    }
                )
            }
        } else {
            send(Resource.Success(entities.map(toDomain)))
        }
    }
}

/** Saves the answer of the backend; a failure is sent as [Resource.Error] only if nothing is shown yet. */
private suspend fun <Remote : Any, Domain : Any> ProducerScope<Resource<Domain>>.refresh(
    hasSaved: Boolean,
    remoteCall: suspend () -> Response<Remote>,
    save: suspend (Remote) -> Unit
) {
    when (val response = remoteCall()) {
        is Response.Success -> try {
            save(response.data)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            if (!hasSaved) send(Resource.Error(ResponseError(message = exception.message ?: "Unknown error")))
        }
        is Response.Error -> if (!hasSaved) send(Resource.Error(response.errorData))
    }
}