package org.kimoteru.wallify.feature.settings.presentation.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassCard
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassStat
import org.kimoteru.wallify.core.presentation.settings.ImageQualityUi

/** A titled card of the settings. */
@Composable
internal fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = appTypography.caps.semiBold,
            color = appColor.text.subtitle
        )

        Column(
            modifier = Modifier.padding(top = appPadding.medium),
            verticalArrangement = Arrangement.spacedBy(appPadding.medium)
        ) {
            content()
        }
    }
}

/** A caption over a segmented choice of an image quality. */
@Composable
internal fun ImageQualityChoice(
    title: String,
    qualities: List<ImageQualityUi>,
    selected: ImageQualityUi,
    onSelect: (ImageQualityUi) -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(appPadding.small)) {
        Text(
            text = title.uppercase(),
            style = appTypography.caps.semiBold,
            color = appColor.text.subtitle
        )

        GlassSegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = qualities.map { quality -> stringResource(quality.label) },
            selectedIndex = qualities.indexOf(selected),
            onSelect = { index -> onSelect(qualities[index]) }
        )
    }
}

/** Two statistics side by side, of the same height. */
@Composable
internal fun StatsRow(
    firstValue: String,
    firstLabel: String,
    secondValue: String,
    secondLabel: String
) {
    val appPadding = AppTheme.padding

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small)
    ) {
        GlassStat(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = firstValue,
            label = firstLabel
        )

        GlassStat(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = secondValue,
            label = secondLabel
        )
    }
}