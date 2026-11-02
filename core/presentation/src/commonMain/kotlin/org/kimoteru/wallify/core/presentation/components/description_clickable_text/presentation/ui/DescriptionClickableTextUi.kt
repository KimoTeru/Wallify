package org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui.DescriptionClickableTextContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui.DescriptionClickableTextContract.UiState

interface DescriptionClickableTextContract : BaseContract {
    @Immutable
    class UiState : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object NavigateOnPrivacyPolicy : UiEvent
        data object NavigateOnTermsOfService : UiEvent
        data object NavigateOnSubscriptionPolicy : UiEvent
    }
}

@Composable
fun DescriptionClickableText(
    modifier: Modifier = Modifier,
    uiState: UiState,
    obtainEvent: (UiEvent) -> Unit,
    textColor: Color = AppTheme.color.text.reversed
) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    val termsText = "Terms of Service"
    val privacyPolicyText = "Privacy Policy"
    val subscriptionPolicyText = "Subscription Terms"

    val fullText =
        "By continuing, you accept our $termsText, $privacyPolicyText and $subscriptionPolicyText."

    val clickableLinks: Map<String, () -> Unit> = mapOf(
        termsText to { obtainEvent(UiEvent.NavigateOnTermsOfService) },
        privacyPolicyText to { obtainEvent(UiEvent.NavigateOnPrivacyPolicy) },
        subscriptionPolicyText to { obtainEvent(UiEvent.NavigateOnSubscriptionPolicy) }
    )

    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = textColor)) {
            append(fullText)
        }
        clickableLinks.keys.forEach { link ->
            val start = fullText.indexOf(link)
            if (start >= 0) {
                addStyle(
                    style = SpanStyle(
                        color = appColor.basic.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = start,
                    end = start + link.length
                )
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(clickableLinks) {
                detectTapGestures { offset ->
                    val layout = textLayoutResult ?: return@detectTapGestures
                    val position = layout.getOffsetForPosition(offset)
                    clickableLinks.forEach { (link, action) ->
                        val start = fullText.indexOf(link)
                        if (start >= 0 && position in start until start + link.length) {
                            action()
                        }
                    }
                }
            },
        text = annotatedString,
        style = appTypography.h6.regular.copy(textAlign = TextAlign.Center),
        onTextLayout = { textLayoutResult = it }
    )
}