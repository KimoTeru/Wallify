package org.kimoteru.wallify.feature.feed.presentation.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassChip
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.presentation.photo.ContentFilterUi
import org.kimoteru.wallify.core.presentation.photo.SearchOrderUi
import org.kimoteru.wallify.core.presentation.photo.toDomain
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_apply
import org.kimoteru.wallify.core.resources.generated.resources.common_reset
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_color
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_content
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_order
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_orientation
import org.kimoteru.wallify.core.resources.generated.resources.feed_filters_note
import org.kimoteru.wallify.core.resources.generated.resources.feed_filters_title
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.UiEvent

/** Search filters over the feed: a bottom sheet on phones, a dialog on wider windows. */
@Composable
internal fun FeedFiltersOverlay(
    isSheet: Boolean,
    uiState: FeedContract.UiState,
    obtainEvent: (UiEvent) -> Unit
) {
    GlassOverlay(
        isSheet = isSheet,
        onDismiss = { obtainEvent(UiEvent.HideFilters) }
    ) { close ->
        FeedFiltersContent(
            uiState = uiState,
            obtainEvent = obtainEvent,
            close = close
        )
    }
}

@Composable
private fun ColumnScope.FeedFiltersContent(
    uiState: FeedContract.UiState,
    obtainEvent: (UiEvent) -> Unit,
    close: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography
    val filters = uiState.draftFilters

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(appPadding.largeSpecial)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.feed_filters_title),
                style = appTypography.h4.semiBold,
                color = appColor.text.title
            )

            GlassIconButton(
                icon = AppIcons.Close,
                onClick = close
            )
        }

        Text(
            text = stringResource(Res.string.feed_filters_note),
            style = appTypography.h6.regular,
            color = appColor.text.subtitle
        )

        FilterSection(title = stringResource(Res.string.feed_filter_orientation)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
                verticalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
            ) {
                uiState.orientations.forEach { orientation ->
                    GlassChip(
                        text = stringResource(orientation.label),
                        selected = filters.orientation == orientation.toDomain(),
                        icon = orientation.icon,
                        onClick = { obtainEvent(UiEvent.ToggleOrientation(orientation.toDomain())) }
                    )
                }
            }
        }

        FilterSection(title = stringResource(Res.string.feed_filter_color)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
                verticalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
            ) {
                uiState.colors.forEach { color ->
                    GlassChip(
                        text = stringResource(color.label),
                        selected = filters.color == color.toDomain(),
                        onClick = { obtainEvent(UiEvent.ToggleColor(color.toDomain())) }
                    )
                }
            }
        }

        FilterSection(title = stringResource(Res.string.feed_filter_order)) {
            GlassSegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = uiState.orders.map { order -> stringResource(order.label) },
                selectedIndex = uiState.orders.indexOfFirst { order ->
                    order.toDomain() == (filters.order ?: SearchOrderUi.Relevant.toDomain())
                },
                onSelect = { index ->
                    obtainEvent(UiEvent.SelectOrder(uiState.orders[index].toDomain()))
                }
            )
        }

        FilterSection(title = stringResource(Res.string.feed_filter_content)) {
            GlassSegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = uiState.contentFilters.map { filter -> stringResource(filter.label) },
                selectedIndex = uiState.contentFilters.indexOfFirst { filter ->
                    filter.toDomain() == (filters.contentFilter ?: ContentFilterUi.Low.toDomain())
                },
                onSelect = { index ->
                    obtainEvent(UiEvent.SelectContentFilter(uiState.contentFilters[index].toDomain()))
                }
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = appPadding.large),
        horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
    ) {
        GlassButton(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.common_reset),
            style = GlassButtonStyle.Ghost,
            onClick = { obtainEvent(UiEvent.ResetFilters) }
        )

        GlassButton(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.common_apply),
            style = GlassButtonStyle.Solid,
            onClick = {
                obtainEvent(UiEvent.ApplyFilters)
                close()
            }
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
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

        content()
    }
}