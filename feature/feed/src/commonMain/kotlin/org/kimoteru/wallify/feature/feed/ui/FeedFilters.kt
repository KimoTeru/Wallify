package org.kimoteru.wallify.feature.feed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassChip
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.color_black
import org.kimoteru.wallify.core.resources.generated.resources.color_black_and_white
import org.kimoteru.wallify.core.resources.generated.resources.color_blue
import org.kimoteru.wallify.core.resources.generated.resources.color_green
import org.kimoteru.wallify.core.resources.generated.resources.color_magenta
import org.kimoteru.wallify.core.resources.generated.resources.color_orange
import org.kimoteru.wallify.core.resources.generated.resources.color_purple
import org.kimoteru.wallify.core.resources.generated.resources.color_red
import org.kimoteru.wallify.core.resources.generated.resources.color_teal
import org.kimoteru.wallify.core.resources.generated.resources.color_white
import org.kimoteru.wallify.core.resources.generated.resources.color_yellow
import org.kimoteru.wallify.core.resources.generated.resources.common_apply
import org.kimoteru.wallify.core.resources.generated.resources.common_reset
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_high
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_low
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_color
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_content
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_order
import org.kimoteru.wallify.core.resources.generated.resources.feed_filter_orientation
import org.kimoteru.wallify.core.resources.generated.resources.feed_filters_note
import org.kimoteru.wallify.core.resources.generated.resources.feed_filters_title
import org.kimoteru.wallify.core.resources.generated.resources.order_latest
import org.kimoteru.wallify.core.resources.generated.resources.order_relevant
import org.kimoteru.wallify.core.resources.generated.resources.orientation_landscape
import org.kimoteru.wallify.core.resources.generated.resources.orientation_portrait
import org.kimoteru.wallify.core.resources.generated.resources.orientation_squarish

/** Search filters over the feed: a bottom sheet on phones, a dialog on wider windows. */
@Composable
fun FeedFiltersOverlay(
    isSheet: Boolean,
    filters: FeedContract.SearchFilters,
    obtainEvent: (FeedContract.UiEvent) -> Unit
) {
    GlassOverlay(
        isSheet = isSheet,
        onDismiss = { obtainEvent(FeedContract.UiEvent.HideFilters) }
    ) { close ->
        FeedFiltersContent(
            filters = filters,
            obtainEvent = obtainEvent,
            close = close
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.FeedFiltersContent(
    filters: FeedContract.SearchFilters,
    obtainEvent: (FeedContract.UiEvent) -> Unit,
    close: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val orderOptions = listOf(SearchOrder.Relevant, SearchOrder.Latest)
    val contentOptions = listOf(ContentFilter.Low, ContentFilter.High)

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
                PhotoOrientation.entries.forEach { orientation ->
                    val isSelected = filters.orientation == orientation

                    GlassChip(
                        text = stringResource(orientation.labelRes()),
                        selected = isSelected,
                        icon = orientation.icon(),
                        onClick = {
                            obtainEvent(
                                FeedContract.UiEvent.SelectOrientation(if (isSelected) null else orientation)
                            )
                        }
                    )
                }
            }
        }

        FilterSection(title = stringResource(Res.string.feed_filter_color)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
                verticalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
            ) {
                PhotoColor.entries.forEach { color ->
                    val isSelected = filters.color == color

                    GlassChip(
                        text = stringResource(color.labelRes()),
                        selected = isSelected,
                        onClick = {
                            obtainEvent(FeedContract.UiEvent.SelectColor(if (isSelected) null else color))
                        }
                    )
                }
            }
        }

        FilterSection(title = stringResource(Res.string.feed_filter_order)) {
            GlassSegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = listOf(
                    stringResource(Res.string.order_relevant),
                    stringResource(Res.string.order_latest)
                ),
                selectedIndex = orderOptions.indexOf(filters.order ?: SearchOrder.Relevant),
                onSelect = { index -> obtainEvent(FeedContract.UiEvent.SelectOrder(orderOptions[index])) }
            )
        }

        FilterSection(title = stringResource(Res.string.feed_filter_content)) {
            GlassSegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = listOf(
                    stringResource(Res.string.content_filter_low),
                    stringResource(Res.string.content_filter_high)
                ),
                selectedIndex = contentOptions.indexOf(filters.contentFilter ?: ContentFilter.Low),
                onSelect = { index ->
                    obtainEvent(FeedContract.UiEvent.SelectContentFilter(contentOptions[index]))
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
            onClick = { obtainEvent(FeedContract.UiEvent.ResetFilters) }
        )

        GlassButton(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.common_apply),
            style = GlassButtonStyle.Solid,
            onClick = {
                obtainEvent(FeedContract.UiEvent.ApplyFilters)
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

private fun PhotoOrientation.labelRes(): StringResource = when (this) {
    PhotoOrientation.Landscape -> Res.string.orientation_landscape
    PhotoOrientation.Portrait -> Res.string.orientation_portrait
    PhotoOrientation.Squarish -> Res.string.orientation_squarish
}

private fun PhotoOrientation.icon(): ImageVector = when (this) {
    PhotoOrientation.Landscape -> AppIcons.CropLandscape
    PhotoOrientation.Portrait -> AppIcons.CropPortrait
    PhotoOrientation.Squarish -> AppIcons.CropSquare
}

private fun PhotoColor.labelRes(): StringResource = when (this) {
    PhotoColor.BlackAndWhite -> Res.string.color_black_and_white
    PhotoColor.Black -> Res.string.color_black
    PhotoColor.White -> Res.string.color_white
    PhotoColor.Yellow -> Res.string.color_yellow
    PhotoColor.Orange -> Res.string.color_orange
    PhotoColor.Red -> Res.string.color_red
    PhotoColor.Purple -> Res.string.color_purple
    PhotoColor.Magenta -> Res.string.color_magenta
    PhotoColor.Green -> Res.string.color_green
    PhotoColor.Teal -> Res.string.color_teal
    PhotoColor.Blue -> Res.string.color_blue
}