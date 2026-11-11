package org.kimoteru.wallify.feature.settings.presentation.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.toCompactString
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.chart.BarChart
import org.kimoteru.wallify.core.designsystem.ui.chart.ShareBar
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_end
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_max
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_start
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_title
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_days_total
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_share

private const val PERCENT = 100

/** Requests per day for the last days with their sum; today is the highlighted last bar. */
@Composable
internal fun RequestDays(
    days: List<Long>,
    total: Long,
    max: Long
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val totalText = total.toCompactString()
    val maxText = max.toCompactString()

    Column(verticalArrangement = Arrangement.spacedBy(appPadding.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(appPadding.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.settings_requests_days_title).uppercase(),
                style = appTypography.caps.semiBold,
                color = appColor.text.subtitle
            )

            Text(
                text = stringResource(Res.string.settings_requests_days_total, totalText),
                style = appTypography.h7.medium,
                color = appColor.text.title,
                maxLines = 1
            )
        }

        BarChart(
            modifier = Modifier.fillMaxWidth(),
            values = days,
            startLabel = stringResource(Res.string.settings_requests_days_start),
            endLabel = stringResource(Res.string.settings_requests_days_end),
            maxLabel = stringResource(Res.string.settings_requests_days_max, maxText),
            contentDescription = stringResource(Res.string.settings_requests_days_description, totalText, maxText)
        )
    }
}

/** An endpoint with its number of requests and a bar of its share among all requests. */
@Composable
internal fun EndpointShare(
    endpoint: String,
    count: Long,
    total: Long
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val fraction = if (total > 0L) count.toFloat() / total else 0f
    val percent = (fraction * PERCENT).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(appPadding.smallSpecial)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(appPadding.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = endpoint,
                style = appTypography.h7.medium,
                color = appColor.text.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(Res.string.settings_requests_share, count.toCompactString(), percent.toString()),
                style = appTypography.h7.regular,
                color = appColor.text.subtitle,
                maxLines = 1
            )
        }

        ShareBar(fraction = fraction)
    }
}