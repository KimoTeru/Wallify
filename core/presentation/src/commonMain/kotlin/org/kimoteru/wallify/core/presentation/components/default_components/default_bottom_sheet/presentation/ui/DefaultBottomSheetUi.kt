package org.kimoteru.wallify.core.presentation.components.default_components.default_bottom_sheet.presentation.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.extensions.mapRange
import org.kimoteru.wallify.core.common.extensions.quarter
import org.kimoteru.wallify.core.designsystem.extensions.quarter
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.HidingVerticalTopAnimated
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateDp
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurLevel
import org.kimoteru.wallify.core.designsystem.ui.blur.blurBackdrop
import org.kimoteru.wallify.core.designsystem.ui.button.DefaultTextButton
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.default_components.default_bottom_sheet.presentation.ui.DefaultBottomSheetContract.UiEvent
import org.kimoteru.wallify.core.presentation.ui.PlatformBackHandler

interface DefaultBottomSheetContract : BaseContract {
    @Immutable
    data class UiState(
        val isVisible: Boolean = false,
        val offsetY: Dp = Dp.Hairline,
        val sheetHeight: Dp = Dp.Hairline,
        val isRefreshAnimatedOffset: Boolean = false,
        val primaryButtonEnabled: Boolean = true
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object Show : UiEvent
        data object Close : UiEvent
        data class UpdateIsRefreshAnimatedOffset(val isRefresh: Boolean) : UiEvent
        data class UpdateOffsetY(val y: Dp) : UiEvent
        data class UpdateSheetHeight(val height: Dp) : UiEvent
    }
}

@Composable
fun DefaultBottomSheet(
    modifier: Modifier = Modifier,
    uiState: DefaultBottomSheetContract.UiState,
    obtainUiEvent: (UiEvent) -> Unit,
    title: String? = null,
    primaryButtonText: String? = null,
    primaryButtonColor: Color = AppTheme.color.basic.primary,
    primaryButtonEnabled: Boolean = true,
    onPrimaryClick: (() -> Unit)? = null,
    includeBottomPadding: Boolean = true,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = AppTheme.padding.largeSpecial),
    durationAnim: Int = AppTheme.durationAnim.small,
    blurLevel: BlurLevel = BlurLevel.Medium,
    content: LazyListScope.(onRequestScroll: () -> Unit) -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appCorner = AppTheme.corner
    val appSize = AppTheme.size
    val appDurationAnim = AppTheme.durationAnim
    val appZIndex = AppTheme.zIndex
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var localOffsetY by remember { mutableStateOf(uiState.offsetY) }
    var animatedTargetOffsetY by remember { mutableStateOf(uiState.offsetY) }

    val animatedOffsetY = animateDp(
        animatedTargetOffsetY,
        animationSpec = tween(if (uiState.isRefreshAnimatedOffset) appDurationAnim.small else 0),
        finishedListener = {
            if (it >= uiState.sheetHeight) obtainUiEvent(UiEvent.Close)
        }
    )

    PlatformBackHandler(enabled = uiState.isVisible) {
        obtainUiEvent(UiEvent.Close)
    }

    LaunchedEffect(uiState.isRefreshAnimatedOffset) {
        if (uiState.isRefreshAnimatedOffset) {
            animatedTargetOffsetY =
                if (uiState.offsetY > uiState.sheetHeight.quarter) uiState.sheetHeight
                else Dp.Hairline
            obtainUiEvent(UiEvent.UpdateOffsetY(animatedTargetOffsetY))
        }
    }

    LaunchedEffect(uiState.offsetY) {
        if (!uiState.isRefreshAnimatedOffset) animatedTargetOffsetY = uiState.offsetY
    }

    val offsetY = if (uiState.isRefreshAnimatedOffset) animatedOffsetY else uiState.offsetY
    val alpha = offsetY.value.mapRange(
        fromMin = uiState.sheetHeight.value, fromMax = Dp.Hairline.value,
        toMin = 0f, toMax = appAlpha.full
    )
    HidingVerticalTopAnimated(
        modifier = Modifier
            .blurBackdrop(
                level = blurLevel,
                alpha = alpha
            )
            .debouncedClickable(enabled = false)
            .zIndex(appZIndex.bottomSheet),
        visible = uiState.isVisible,
        duration = durationAnim
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = appSize.panel.sheetMaxWidth)
                    .fillMaxWidth()
                    .applyIf(uiState.sheetHeight == Dp.Hairline) {
                        onSizeChanged {
                            obtainUiEvent(
                                UiEvent.UpdateSheetHeight(
                                    with(density) { it.height.toDp() }
                                )
                            )
                        }
                    }
                    .offset(y = offsetY)
                    .clip(
                        RoundedCornerShape(
                            topStart = appCorner.medium,
                            topEnd = appCorner.medium
                        )
                    )
                    .background(appColor.basic.background)
                    .applyIfNotNull {
                        uiState.sheetHeight?.let {
                            pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        localOffsetY = uiState.offsetY
                                        obtainUiEvent(
                                            UiEvent.UpdateIsRefreshAnimatedOffset(false)
                                        )
                                    },
                                    onDragEnd = {
                                        obtainUiEvent(
                                            UiEvent.UpdateIsRefreshAnimatedOffset(true)
                                        )
                                    }
                                ) { change, dragAmount ->
                                    val delta = dragAmount.y.toDp()

                                    if (localOffsetY <= 0.dp && delta < 0.dp) return@detectDragGestures

                                    localOffsetY += delta
                                    obtainUiEvent(
                                        UiEvent.UpdateOffsetY(localOffsetY)
                                    )
                                    change.consume()
                                }
                            }
                        }
                    }
                    .padding(
                        top = appPadding.smallSpecial,
                        start = appPadding.medium,
                        end = appPadding.medium,
                        bottom = if (includeBottomPadding) appPadding.medium else Dp.Hairline
                    )
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(appCorner.smallSpecial))
                            .background(appColor.icons.care)
                            .size(appSize.bottomSheet.drag)
                    )

                    DefaultTopBar(
                        modifier = Modifier.padding(top = appPadding.small),
                        title = title,
                        paddingValues = PaddingValues(),
                        onClose = {
                            obtainUiEvent(UiEvent.Close)
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(contentPaddingValues)
                    ) {
                        LazyColumn(
                            modifier = modifier,
                            state = scrollState
                        ) {
                            content(this) {
                                coroutineScope.launch {
                                    scrollState.animateScrollBy(Int.MAX_VALUE.toFloat())
                                }
                            }
                        }
                    }

                    if (primaryButtonText != null && onPrimaryClick != null) {
                        DefaultTextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            text = primaryButtonText,
                            backgroundColor = primaryButtonColor,
                            enabled = primaryButtonEnabled && uiState.primaryButtonEnabled,
                            onClick = onPrimaryClick
                        )
                    }
                }
            }
        }
    }
}