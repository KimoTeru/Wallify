package org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Velocity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_refreshing

private const val REFRESH_LINE_KEY = "refresh_line"
private const val HIGHLIGHT_BAND_FRACTION = 0.35f
private const val HIGHLIGHT_STILL_PROGRESS = 0.5f
private const val FALLBACK_IDLE_MILLIS = 200
private const val REFRESH_START_WAIT_MILLIS = 1_000L

/**
 * Requests to refresh the list on screen that do not come from a gesture (the F5 and Cmd/Ctrl+R keys of the desktop
 * window). Empty by default: the app that has such keys provides them in `AppTheme`.
 */
val LocalRefreshRequests = staticCompositionLocalOf<Flow<Unit>> { emptyFlow() }

/**
 * The line at the top of a list that shows a refresh: an item of the grid whose height grows with the pull, stays
 * while the list refreshes (whoever started it: the user or the age of the list) and shrinks to nothing when it is
 * done. Made by [rememberRefreshLine], drawn by `PhotoGridLayout`.
 */
@Stable
class RefreshLine internal constructor(
    internal val modifier: Modifier,
    /** How much of the area the line takes, 0…1. */
    internal val fraction: State<Float>,
    internal val isRefreshing: State<Boolean>
)

/**
 * Pull to refresh as a line item of a list. A downward scroll the list cannot take at its top edge pulls the line
 * ([PullTracker]); letting go past the threshold asks for a refresh, a shorter pull collapses back. A touch gesture
 * lets go at its fling; a desktop wheel or trackpad has no such end, so the pull is let go when no scroll arrives for
 * a moment ([isPullReleasedOnIdle]). [isRefreshing] is the state of the list, so a refresh by age of the list shows
 * the same line. The keys of the desktop window come through [LocalRefreshRequests].
 */
@Composable
fun rememberRefreshLine(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
): RefreshLine {
    val appDuration = AppTheme.durationAnim
    val appSize = AppTheme.size

    val scope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { appSize.refreshLine.threshold.toPx() }
    val idleMillis = appDuration.smallExtra.takeIf { it > 0 } ?: FALLBACK_IDLE_MILLIS
    val refreshing = rememberUpdatedState(isRefreshing)
    val currentOnRefresh = rememberUpdatedState(onRefresh)
    val requests = LocalRefreshRequests.current

    val pullFraction = remember { mutableFloatStateOf(0f) }
    val refreshProgress = animateFloatAsState(
        targetValue = if (isRefreshing) 1f else 0f,
        animationSpec = tween(durationMillis = appDuration.small),
        label = "refreshLineProgress"
    )
    val fraction = remember {
        derivedStateOf { max(pullFraction.floatValue.coerceIn(0f, 1f), refreshProgress.value) }
    }

    val connection = remember(thresholdPx, idleMillis, appDuration.small) {
        val mark = TimeSource.Monotonic.markNow()

        PullConnection(
            tracker = PullTracker(
                thresholdPx = thresholdPx,
                releasesOnIdle = isPullReleasedOnIdle,
                idleMillis = idleMillis.toLong()
            ),
            scope = scope,
            pullFraction = pullFraction,
            settleMillis = appDuration.small,
            isRefreshing = { refreshing.value },
            onRefresh = { currentOnRefresh.value() },
            nowMs = { mark.elapsedNow().inWholeMilliseconds }
        )
    }

    LaunchedEffect(requests) {
        requests.collect {
            if (!refreshing.value) currentOnRefresh.value()
        }
    }

    return RefreshLine(
        modifier = Modifier.nestedScroll(connection),
        fraction = fraction,
        isRefreshing = refreshing
    )
}

/**
 * Feeds [tracker] with the scroll the list does not take: pulls on a downward one at the top edge, retracts on an
 * upward one while pulled, and lets go at a fling (a touch) or after the idle time (a wheel, a trackpad).
 */
private class PullConnection(
    private val tracker: PullTracker,
    private val scope: CoroutineScope,
    private val pullFraction: MutableFloatState,
    private val settleMillis: Int,
    private val isRefreshing: () -> Boolean,
    private val onRefresh: () -> Unit,
    private val nowMs: () -> Long
) : NestedScrollConnection {

    private var settleJob: Job? = null
    private var idleJob: Job? = null

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput || available.y >= 0f || tracker.distance <= 0f) return Offset.Zero

        settleJob?.cancel()
        val taken = tracker.retract(delta = available.y, nowMs = nowMs())
        pullFraction.floatValue = tracker.fraction
        scheduleIdleRelease()

        return Offset(0f, taken)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput || available.y <= 0f || isRefreshing()) return Offset.Zero

        settleJob?.cancel()
        val taken = tracker.pull(delta = available.y, nowMs = nowMs())
        pullFraction.floatValue = tracker.fraction
        scheduleIdleRelease()

        return Offset(0f, taken)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (tracker.distance <= 0f) return Velocity.Zero

        release()

        return Velocity(0f, available.y)
    }

    private fun scheduleIdleRelease() {
        idleJob?.cancel()

        if (!tracker.releasesOnIdle) return

        idleJob = scope.launch {
            delay(tracker.idleMillis)
            if (tracker.isIdle(nowMs())) release()
        }
    }

    private fun release() {
        idleJob?.cancel()

        if (settleJob?.isActive == true || tracker.distance <= 0f) return

        val isTriggered = tracker.release()

        if (isTriggered) onRefresh()

        settleJob = scope.launch {
            if (isTriggered) {
                // Held at the line until the list reports the refresh, otherwise the line would dip and come back.
                tracker.settle(min(tracker.fraction, 1f))
                pullFraction.floatValue = tracker.fraction
                withTimeoutOrNull(REFRESH_START_WAIT_MILLIS) { snapshotFlow { isRefreshing() }.first { it } }
            }

            animate(
                initialValue = tracker.fraction,
                targetValue = 0f,
                animationSpec = tween(durationMillis = settleMillis)
            ) { value, _ ->
                tracker.settle(value)
                pullFraction.floatValue = value
            }
        }
    }
}

/** The line as the first item of a grid: zero high while idle, so it never has to be inserted above the cards. */
internal fun LazyStaggeredGridScope.refreshLineItem(line: RefreshLine) {
    item(
        key = REFRESH_LINE_KEY,
        span = StaggeredGridItemSpan.FullLine,
        contentType = REFRESH_LINE_KEY
    ) {
        RefreshLineContent(line = line)
    }
}

@Composable
private fun RefreshLineContent(line: RefreshLine) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appDuration = AppTheme.durationAnim
    val appSize = AppTheme.size

    val isRefreshing by line.isRefreshing
    val description = stringResource(Res.string.common_refreshing)

    // The band runs only while refreshing (a running transition redraws every frame); without animations it stands
    // still in the middle of the line.
    val highlight = if (isRefreshing && appDuration.small > 0) rememberHighlightProgress(appDuration.large) else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .layout { measurable, constraints ->
                val area = appSize.refreshLine.area.roundToPx()
                val placeable = measurable.measure(constraints.copy(minHeight = area, maxHeight = area))
                val height = (area * line.fraction.value).roundToInt()

                layout(placeable.width, height) { placeable.place(0, 0) }
            }
            .applyIf(isRefreshing) {
                semantics {
                    contentDescription = description
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(appSize.refreshLine.thickness)
                .graphicsLayer { scaleX = line.fraction.value }
                .clip(CircleShape)
                .background(appColor.basic.primary.copy(alpha = appAlpha.small))
                .applyIf(isRefreshing) {
                    runningHighlight(
                        color = appColor.basic.primary,
                        progress = { highlight?.value ?: HIGHLIGHT_STILL_PROGRESS }
                    )
                }
        )
    }
}

@Composable
private fun rememberHighlightProgress(durationMillis: Int): State<Float> =
    rememberInfiniteTransition(label = "refreshLineHighlight").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshLineHighlightProgress"
    )

/** A band of [color] that fades out on both sides and runs along the box; the progress is read only when drawing. */
private fun Modifier.runningHighlight(
    color: Color,
    progress: () -> Float
): Modifier = drawWithCache {
    val bandWidth = size.width * HIGHLIGHT_BAND_FRACTION
    val band = Brush.horizontalGradient(
        colors = listOf(Color.Transparent, color, Color.Transparent),
        startX = 0f,
        endX = bandWidth
    )

    onDrawWithContent {
        drawContent()
        translate(left = -bandWidth + (size.width + bandWidth) * progress()) {
            drawRect(brush = band, size = Size(bandWidth, size.height))
        }
    }
}