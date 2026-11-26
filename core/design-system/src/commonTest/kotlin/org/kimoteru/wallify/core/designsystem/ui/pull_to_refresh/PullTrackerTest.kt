package org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PullTrackerTest {

    private fun tracker(releasesOnIdle: Boolean = false) = PullTracker(
        thresholdPx = 100f,
        releasesOnIdle = releasesOnIdle,
        idleMillis = 200L
    )

    @Test
    fun pullGrowsTheFractionDamped() {
        val tracker = tracker()

        tracker.pull(delta = 100f, nowMs = 0L)

        assertEquals(100f, tracker.distance)
        assertEquals(0.5f, tracker.fraction)
    }

    @Test
    fun pullIsCapped() {
        val tracker = tracker()

        tracker.pull(delta = 10_000f, nowMs = 0L)

        assertEquals(1.5f, tracker.fraction)
    }

    @Test
    fun retractTakesBackOnlyWhatWasPulled() {
        val tracker = tracker()
        tracker.pull(delta = 100f, nowMs = 0L)

        assertEquals(-40f, tracker.retract(delta = -40f, nowMs = 1L))
        assertEquals(60f, tracker.distance)
        assertEquals(-60f, tracker.retract(delta = -100f, nowMs = 2L))
        assertEquals(0f, tracker.distance)
    }

    @Test
    fun releasePastThresholdAsksForRefresh() {
        val tracker = tracker()
        tracker.pull(delta = 200f, nowMs = 0L)

        assertTrue(tracker.release())
    }

    @Test
    fun releaseBelowThresholdDoesNot() {
        val tracker = tracker()
        tracker.pull(delta = 199f, nowMs = 0L)

        assertFalse(tracker.release())
    }

    @Test
    fun settleMovesTheDistanceToTheFraction() {
        val tracker = tracker()
        tracker.pull(delta = 200f, nowMs = 0L)

        tracker.settle(fraction = 0.5f)

        assertEquals(100f, tracker.distance)
    }

    @Test
    fun pulledListIsReleasedAfterIdleOnDesktop() {
        val tracker = tracker(releasesOnIdle = true)
        tracker.pull(delta = 50f, nowMs = 1_000L)

        assertFalse(tracker.isIdle(nowMs = 1_199L))
        assertTrue(tracker.isIdle(nowMs = 1_200L))
    }

    @Test
    fun newPullRestartsTheIdleTime() {
        val tracker = tracker(releasesOnIdle = true)
        tracker.pull(delta = 50f, nowMs = 0L)
        tracker.pull(delta = 50f, nowMs = 150L)

        assertFalse(tracker.isIdle(nowMs = 300L))
        assertTrue(tracker.isIdle(nowMs = 350L))
    }

    @Test
    fun noIdleReleaseWithoutThePlatformFlag() {
        val tracker = tracker(releasesOnIdle = false)
        tracker.pull(delta = 50f, nowMs = 0L)

        assertFalse(tracker.isIdle(nowMs = 10_000L))
    }

    @Test
    fun nothingPulledIsNeverIdle() {
        val tracker = tracker(releasesOnIdle = true)

        assertFalse(tracker.isIdle(nowMs = 10_000L))
    }
}