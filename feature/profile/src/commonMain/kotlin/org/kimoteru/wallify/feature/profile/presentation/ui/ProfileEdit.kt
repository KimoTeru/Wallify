package org.kimoteru.wallify.feature.profile.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animatedHeight
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassTextField
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_bio
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_email
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_error
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_first_name
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_instagram
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_last_name
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_location
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_portfolio
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_save
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_title
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit_username

private const val BIO_MIN_LINES = 3
private const val BIO_MAX_LINES = 6

/** Form to change the own profile: a bottom sheet on phones, a dialog on wider windows. */
@Composable
fun ProfileEditOverlay(
    isSheet: Boolean,
    form: EditForm,
    isSaving: Boolean,
    hasSaveError: Boolean,
    obtainEvent: (ProfileContract.UiEvent) -> Unit
) {
    GlassOverlay(
        isSheet = isSheet,
        onDismiss = { obtainEvent(ProfileContract.UiEvent.HideEdit) }
    ) { close ->
        ProfileEditContent(
            form = form,
            isSaving = isSaving,
            hasSaveError = hasSaveError,
            obtainEvent = obtainEvent,
            close = close
        )
    }
}

@Composable
private fun ColumnScope.ProfileEditContent(
    form: EditForm,
    isSaving: Boolean,
    hasSaveError: Boolean,
    obtainEvent: (ProfileContract.UiEvent) -> Unit,
    close: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val change: (EditField) -> (String) -> Unit = { field ->
        { value -> obtainEvent(ProfileContract.UiEvent.ChangeEditField(field, value)) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = appPadding.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.profile_edit_title),
            style = appTypography.h4.semiBold,
            color = appColor.text.title
        )

        GlassIconButton(
            icon = AppIcons.Close,
            onClick = close
        )
    }

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(appPadding.medium)
    ) {

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.username,
            onValueChange = change(EditField.Username),
            label = stringResource(Res.string.profile_edit_username),
            enabled = !isSaving
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.firstName,
            onValueChange = change(EditField.FirstName),
            label = stringResource(Res.string.profile_edit_first_name),
            enabled = !isSaving
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.lastName,
            onValueChange = change(EditField.LastName),
            label = stringResource(Res.string.profile_edit_last_name),
            enabled = !isSaving
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.email,
            onValueChange = change(EditField.Email),
            label = stringResource(Res.string.profile_edit_email),
            enabled = !isSaving,
            keyboardType = KeyboardType.Email
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.portfolio,
            onValueChange = change(EditField.Portfolio),
            label = stringResource(Res.string.profile_edit_portfolio),
            enabled = !isSaving,
            keyboardType = KeyboardType.Uri
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.location,
            onValueChange = change(EditField.Location),
            label = stringResource(Res.string.profile_edit_location),
            enabled = !isSaving
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.instagram,
            onValueChange = change(EditField.Instagram),
            label = stringResource(Res.string.profile_edit_instagram),
            enabled = !isSaving
        )

        GlassTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.bio,
            onValueChange = change(EditField.Bio),
            label = stringResource(Res.string.profile_edit_bio),
            enabled = !isSaving,
            minLines = BIO_MIN_LINES,
            maxLines = BIO_MAX_LINES
        )
    }

    Text(
        modifier = Modifier
            .animatedHeight(visible = hasSaveError)
            .padding(vertical = appPadding.medium)
            .align(Alignment.CenterHorizontally),
        text = stringResource(Res.string.profile_edit_error),
        style = appTypography.h7.medium,
        color = appColor.basic.error,
        textAlign = TextAlign.Center
    )

    GlassButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = appPadding.medium),
        text = stringResource(Res.string.profile_edit_save),
        style = GlassButtonStyle.Solid,
        enabled = form.isValid && !isSaving,
        onClick = { obtainEvent(ProfileContract.UiEvent.SaveEdit) }
    )
}