package org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * Pull to refresh over [content]. [indicatorPadding] moves the indicator below what floats over the top of the
 * content (the bar of the screen), otherwise it would appear under the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultPullToRefreshBox(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    indicatorPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit
) {
    val appColor = AppTheme.color
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier,
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(indicatorPadding),
                isRefreshing = isRefreshing,
                containerColor = appColor.basic.background,
                color = appColor.basic.primary,
                state = state
            )
        },
        content = content
    )
}