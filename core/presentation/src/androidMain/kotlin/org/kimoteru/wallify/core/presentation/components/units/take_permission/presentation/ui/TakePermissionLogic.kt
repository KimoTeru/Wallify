package org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.awaitUntil
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.platform.managers.intent.IntentNavigator
import org.kimoteru.wallify.core.presentation.base.BaseComponentLogic
import org.kimoteru.wallify.core.presentation.components.units.take_permission.domain.TakePermissionRepository
import org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui.TakePermissionContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.take_permission.presentation.ui.TakePermissionContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

fun KoinComponent.getTakePermissionLogic(
    args: TakePermissionArgs,
    coroutineScope: CoroutineScope,
    onDenyPermission: () -> Unit = {}
): TakePermissionLogic {
    return get {
        parametersOf(
            args,
            coroutineScope,
            onDenyPermission
        )
    }
}

@Factory
class TakePermissionLogic(
    @InjectedParam private val args: TakePermissionArgs,
    @InjectedParam private val coroutineScope: CoroutineScope,
    @InjectedParam private val onDenyPermission: () -> Unit,
    private val repository: TakePermissionRepository,
    private val intentNavigator: IntentNavigator
) : BaseComponentLogic<UiState, UiEvent, Nothing>() {

    val isPermissionGranted: StateFlow<Boolean>
        field = MutableStateFlow(false)

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState(permission = args.permission))

    private var hasRequestedPermission = false

    private val isInitialPermissionGranted = MutableStateFlow<Boolean?>(null)

    init {
        coroutineScope.launch {
            uiState.mapDistinctBy { it.shouldRequestPermission }.collectLatest { }
        }
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.GrantPermission -> grantPermission(event.isGranted)
            is UiEvent.CloseDialog -> {
                updateIsVisibleDialog(false)
                onDenyPermission()
            }
            is UiEvent.ShowDialog -> {
                if (args.isNeedOpenCustomDialog) {
                    updateIsVisibleDialog(true)
                } else {
                    navigateSettings()
                }
            }
            is UiEvent.StartPermissionRequest -> {
                hasRequestedPermission = true
                updateIsVisibleDialog(false)
            }
            is UiEvent.RequestAppSettingsPermission -> navigateSettings()
            is UiEvent.ResetPermissionRequest -> updateShouldRequestPermission(false)
        }
    }

    suspend fun isGrantedInitialPermission(): Boolean {
        isInitialPermissionGranted.awaitUntil { it != null }
        return isInitialPermissionGranted.value ?: false
    }

    fun navigateSettings() {
        when (args.permission) {
            Permission.REMOTE_NOTIFICATION -> intentNavigator.openNotificationSettings()
            else -> intentNavigator.openAppSettings()
        }
    }

    fun requestPermission() {
        updateShouldRequestPermission(true)
    }

    private fun grantPermission(isGranted: Boolean) {
        if (isGranted && args.permission == Permission.REMOTE_NOTIFICATION) {
            coroutineScope.launch {
                repository.tryEnablePushNotify()
            }
        }

        if (isInitialPermissionGranted.value == null) {
            isPermissionGranted.value = isGranted
            isInitialPermissionGranted.value = isGranted
            return
        }

        if (!isGranted && args.permission == Permission.CAMERA && isInitialPermissionGranted.value != null) {
            updateIsVisibleDialog(true)
            return
        }

        updateIsVisibleDialog(false)
        isPermissionGranted.value = isGranted

        if (!isGranted && args.permission != Permission.CAMERA) {
            onDenyPermission()
        }
    }

    private fun updateIsVisibleDialog(isVisible: Boolean) {
        uiState.update { it.copy(isVisibleDialog = isVisible) }
    }

    private fun updateShouldRequestPermission(isRequest: Boolean) {
        uiState.update { it.copy(shouldRequestPermission = isRequest) }
    }
}