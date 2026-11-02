package org.kimoteru.wallify.core.designsystem.ui.dialog

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.HidingVerticalTopAnimated
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateFloat
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurLevel
import org.kimoteru.wallify.core.designsystem.ui.blur.blurBackdrop
import org.kimoteru.wallify.core.designsystem.ui.button.DefaultTextIconButton
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.icon.IconClose
import org.kimoteru.wallify.core.designsystem.ui.icon.iconContainer
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar

@Composable
fun DefaultDialogContainer(
    modifier: Modifier = Modifier,
    blurLevel: BlurLevel = BlurLevel.Medium,
    durationAnim: Int = AppTheme.durationAnim.smallExtra,
    onDismiss: () -> Unit,
    dismissRequested: Boolean = false,
    content: @Composable() (BoxScope.() -> Unit) = {}
) {
    val appAlpha = AppTheme.alpha
    val appZIndex = AppTheme.zIndex

    var isVisible by remember {
        mutableStateOf(false)
    }

    val backdropAlpha = animateFloat(
        targetValue = if (isVisible) appAlpha.full else 0f,
        animationSpec = tween(durationMillis = durationAnim)
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(durationAnim.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(dismissRequested) { if (dismissRequested) isVisible = false }

    HidingVerticalTopAnimated(
        modifier = modifier
            .blurBackdrop(
                level = blurLevel,
                alpha = backdropAlpha
            )
            .systemBarsPadding()
            .zIndex(appZIndex.dialogs),
        visible = isVisible,
        duration = durationAnim
    ) {
        Box(
            modifier =
                Modifier
                    .debouncedClickable(onClick = { isVisible = false }, animateClick = false)
                    .fillMaxSize(),
            content = content
        )
    }
}

@Composable
fun DefaultDialogLayout(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    isVisibleCloseButton: Boolean = true,
    alignment: Alignment = Alignment.BottomCenter,
    content: @Composable (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appAlpha = AppTheme.alpha
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding

    // Lifts the dialog above the keyboard. Tapping outside the card also dismisses it.
    val bottomPadding = (WindowInsets.ime.asPaddingValues().calculateBottomPadding() -
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        appPadding.small).coerceAtLeast(Dp.Hairline)

    var dismissRequested by remember {
        mutableStateOf(false)
    }

    DefaultDialogContainer(
        onDismiss = onDismiss,
        dismissRequested = dismissRequested
    ) {
        Column(
            modifier = modifier
                .align(alignment)
                .padding(horizontal = appPadding.medium)
                .padding(bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .debouncedClickable(animateClick = false)
                    .clip(RoundedCornerShape(appCorner.large))
                    .background(appColor.basic.card.basic)
                    .padding(
                        horizontal = appPadding.medium,
                        vertical = appPadding.largeSpecial
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content?.invoke()
            }

            Spacer(Modifier.padding(top = if (isVisibleCloseButton) appPadding.large else appPadding.medium))

            if (isVisibleCloseButton) {
                IconClose(
                    modifier = Modifier
                        .padding(bottom = appPadding.medium)
                        .iconContainer(
                            color = appColor.basic.white.copy(alpha = appAlpha.small),
                            onClick = { dismissRequested = true }
                        )
                )
            }
        }
    }
}

@Composable
fun DefaultDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = AppTheme.typography.h4.bold,
    description: String? = null,
    onDismiss: () -> Unit,
    primaryButtonText: String? = null,
    primaryButtonColor: Color = AppTheme.color.basic.button.primary,
    primaryButtonEnabled: Boolean = true,
    primaryButtonTextStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.reversed),
    primaryButtonIcon: @Composable (() -> Unit)? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    secondaryButtonColor: Color = Color.Transparent,
    secondaryButtonEnabled: Boolean = true,
    secondaryButtonTextStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.subtitle),
    secondaryButtonIcon: @Composable (() -> Unit)? = null,
    onSecondaryClick: (() -> Unit)? = null,
    tertiaryButtonText: String? = null,
    tertiaryButtonColor: Color = Color.Transparent,
    tertiaryButtonEnabled: Boolean = true,
    tertiaryButtonTextStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.subtitle),
    tertiaryButtonIcon: @Composable (() -> Unit)? = null,
    onTertiaryClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    isVisibleCloseButton: Boolean = true,
    alignment: Alignment = Alignment.BottomCenter,
    isNeedLargePaddingPrimaryButton: Boolean = true,
    headerContent: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    DefaultDialogLayout(
        modifier = modifier,
        onDismiss = onDismiss,
        isVisibleCloseButton = isVisibleCloseButton,
        alignment = alignment
    ) {
        headerContent?.invoke()

        DefaultTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .applyIfNotNull {
                    headerContent?.let {
                        padding(top = appPadding.largeSpecial)
                    }
                },
            title = title,
            titleStyle = titleStyle,
            paddingValues = PaddingValues(),
            onBack = onBackClick
        )

        description?.let {
            Text(
                modifier = Modifier.padding(top = appPadding.small),
                text = description,
                style = appTypography.h5.medium,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }

        content?.invoke()

        if (primaryButtonText != null && onPrimaryClick != null) {
            DefaultTextIconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isNeedLargePaddingPrimaryButton) appPadding.medium else appPadding.largeSpecial),
                text = primaryButtonText,
                icon = primaryButtonIcon,
                enabled = primaryButtonEnabled,
                textStyle = primaryButtonTextStyle,
                backgroundColor = primaryButtonColor,
                onClick = onPrimaryClick
            )
        }

        if (secondaryButtonText != null && onSecondaryClick != null) {
            DefaultTextIconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (primaryButtonText != null && onPrimaryClick != null) appPadding.small else appPadding.largeSpecial),
                text = secondaryButtonText,
                icon = secondaryButtonIcon,
                enabled = secondaryButtonEnabled,
                textStyle = secondaryButtonTextStyle,
                backgroundColor = secondaryButtonColor,
                onClick = onSecondaryClick
            )
        }

        if (tertiaryButtonText != null && onTertiaryClick != null) {
            DefaultTextIconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = appPadding.small),
                text = tertiaryButtonText,
                icon = tertiaryButtonIcon,
                enabled = tertiaryButtonEnabled,
                textStyle = tertiaryButtonTextStyle,
                backgroundColor = tertiaryButtonColor,
                onClick = onTertiaryClick
            )
        }
    }
}

@Composable
fun DefaultDescriptionDialog(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    additionalDescription: String? = null,
    primaryButtonText: String,
    primaryButtonColor: Color = AppTheme.color.basic.primary,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    secondaryButtonTextStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.subtitle),
    onSecondaryClick: (() -> Unit)? = null,
    secondaryButtonColor: Color = Color.Transparent,
    isVisibleCloseButton: Boolean = true,
    onDismiss: () -> Unit
) {
    val appPadding = AppTheme.padding
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    DefaultDialog(
        modifier = modifier,
        title = title,
        onDismiss = onDismiss,
        primaryButtonText = primaryButtonText,
        primaryButtonColor = primaryButtonColor,
        onPrimaryClick = onPrimaryClick,
        secondaryButtonText = secondaryButtonText,
        secondaryButtonTextStyle = secondaryButtonTextStyle,
        secondaryButtonColor = secondaryButtonColor,
        onSecondaryClick = onSecondaryClick,
        isVisibleCloseButton = isVisibleCloseButton,
        content = {
            Text(
                modifier = Modifier.padding(top = appPadding.medium),
                text = description,
                style = appTypography.h6.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )

            additionalDescription?.let {
                Text(
                    modifier = Modifier.padding(top = appPadding.small),
                    text = it,
                    style = appTypography.h6.regular,
                    color = appColor.text.subtitle,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}