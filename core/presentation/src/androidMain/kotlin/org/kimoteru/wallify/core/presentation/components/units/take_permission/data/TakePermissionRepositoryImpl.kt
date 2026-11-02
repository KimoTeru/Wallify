package org.kimoteru.wallify.core.presentation.components.units.take_permission.data

import org.kimoteru.wallify.core.presentation.components.units.take_permission.domain.TakePermissionRepository
import org.koin.core.annotation.Singleton

@Singleton
class TakePermissionRepositoryImpl : TakePermissionRepository {
    override suspend fun tryEnablePushNotify() = Unit
}