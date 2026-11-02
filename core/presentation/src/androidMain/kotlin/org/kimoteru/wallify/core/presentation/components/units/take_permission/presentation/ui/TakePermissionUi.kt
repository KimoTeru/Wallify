package org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.designsystem.ui.compose.LifecycleHandler
import org.kimoteru.wallify.core.designsystem.ui.dialog.DefaultDescriptionDialog
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui.TakePermissionContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui.TakePermissionContract.UiState
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.permission_allow_access
import org.kimoteru.wallify.core.resources.generated.resources.permission_camera
import org.kimoteru.wallify.core.resources.generated.resources.permission_camera_description
import org.kimoteru.wallify.core.resources.generated.resources.permission_denied_title
import org.kimoteru.wallify.core.resources.generated.resources.permission_notifications
import org.kimoteru.wallify.core.resources.generated.resources.permission_notifications_description

interface TakePermissionContract : BaseContract {
    data class UiState(
        val isVisibleDialog: Boolean = false,
        val shouldRequestPermission: Boolean = false,
        val permission: Permission? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object CloseDialog : UiEvent
        data object ShowDialog : UiEvent
        data object StartPermissionRequest : UiEvent
        data object RequestAppSettingsPermission : UiEvent
        data object ResetPermissionRequest : UiEvent
        data class GrantPermission(val isGranted: Boolean) : UiEvent
    }
}

@Serializable
data class TakePermissionArgs(
    val permission: Permission = Permission.REMOTE_NOTIFICATION,
    val isNeedOpenCustomDialog: Boolean = true
)

@Composable
fun TakePermissionUi(
    uiState: UiState,
    obtainUiEvent: (UiEvent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val permissionFactory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val permissionController: PermissionsController = remember(permissionFactory) {
        permissionFactory.createPermissionsController()
    }

    BindEffect(permissionController)

    LaunchedEffect(uiState.shouldRequestPermission) {
        uiState.permission?.let { permission ->
            val state = permissionController.getPermissionState(permission)

            if (uiState.shouldRequestPermission) {
                if (!permissionController.isPermissionGranted(permission)) {
                    try {
                        obtainUiEvent(UiEvent.StartPermissionRequest)
                        permissionController.providePermission(permission)
                        obtainUiEvent(
                            UiEvent.GrantPermission(permissionController.isPermissionGranted(permission))
                        )
                    } catch (_: RequestCanceledException) {
                        obtainUiEvent(UiEvent.ShowDialog)
                    } catch (_: DeniedAlwaysException) {
                        if (state == PermissionState.Denied) {
                            obtainUiEvent(UiEvent.GrantPermission(false))
                        } else {
                            obtainUiEvent(UiEvent.ShowDialog)
                        }
                    } catch (_: DeniedException) {
                        obtainUiEvent(UiEvent.GrantPermission(false))
                    } finally {
                        obtainUiEvent(UiEvent.ResetPermissionRequest)
                    }
                } else {
                    obtainUiEvent(UiEvent.GrantPermission(true))
                }
            }
        }
    }

    uiState.permission?.let { permission ->
        LifecycleHandler(
            onStart = {
                coroutineScope.launch {
                    obtainUiEvent(
                        UiEvent.GrantPermission(
                            permissionController.isPermissionGranted(permission)
                        )
                    )
                }
            }
        )
    }

    if (uiState.isVisibleDialog) {
        uiState.permission?.let { permission ->
            val permissionLabel = when (permission) {
                Permission.CAMERA -> stringResource(Res.string.permission_camera)
                Permission.REMOTE_NOTIFICATION -> stringResource(Res.string.permission_notifications)
                else -> String.Empty
            }

            DefaultDescriptionDialog(
                title = stringResource(Res.string.permission_denied_title, permissionLabel),
                description = when (permission) {
                    Permission.CAMERA -> stringResource(Res.string.permission_camera_description)
                    Permission.REMOTE_NOTIFICATION -> stringResource(Res.string.permission_notifications_description)
                    else -> String.Empty
                },
                primaryButtonText = stringResource(Res.string.permission_allow_access),
                onPrimaryClick = {
                    obtainUiEvent(UiEvent.RequestAppSettingsPermission)
                },
                onDismiss = {
                    obtainUiEvent(UiEvent.CloseDialog)
                }
            )
        }
    }
}