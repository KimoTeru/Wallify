package org.kimoteru.wallify.core.presentation.components.units.take_permission.domain

interface TakePermissionRepository {
    suspend fun tryEnablePushNotify()
}