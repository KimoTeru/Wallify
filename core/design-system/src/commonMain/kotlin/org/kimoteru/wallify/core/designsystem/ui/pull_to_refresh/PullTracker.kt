package org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh

import kotlin.math.max
import kotlin.math.min

private const val DAMPING = 0.5f
private const val MAX_FRACTION = 1.5f

/**
 * How far a list is pulled down at its top edge and when that pull is let go. Plain numbers, no Compose: the nested
 * scroll connection of the line feeds it and draws from it.
 *
 * The pull is damped ([DAMPING]: two pixels of the gesture make one of the pull) and capped at [MAX_FRACTION] of
 * [thresholdPx]. A touch gesture ends with a fling, which releases it; a mouse wheel and a trackpad have no such end,
 * so where [releasesOnIdle] is set a pull that got no new delta for [idleMillis] counts as let go ([isIdle]). On a
 * touch screen a finger held still must not release, so it is not set there.
 */
internal class PullTracker(
    private val thresholdPx: Float,
    val releasesOnIdle: Boolean,
    val idleMillis: Long
) {
    /** Raw distance of the gesture in pixels, 0 when the list is not pulled. */
    var distance: Float = 0f
        private set

    private var lastDeltaMs = 0L

    /** Damped pull in thresholds: 1 is where a release asks for a refresh. */
    val fraction: Float get() = distance * DAMPING / thresholdPx

    /** Adds the unconsumed downward [delta] of the gesture; returns what the pull took of it (all of it). */
    fun pull(delta: Float, nowMs: Long): Float {
        distance = min(distance + delta, MAX_FRACTION * thresholdPx / DAMPING)
        lastDeltaMs = nowMs
        return delta
    }

    /** Takes back the upward [delta] (negative) from the pull first; returns how much of it the pull took. */
    fun retract(delta: Float, nowMs: Long): Float {
        val taken = max(delta, -distance)
        distance += taken
        lastDeltaMs = nowMs
        return taken
    }

    /** `true` when the pull let go at this point asks for a refresh. */
    fun release(): Boolean = fraction >= 1f

    /** Moves the pull to [fraction], for the animation back. */
    fun settle(fraction: Float) {
        distance = fraction * thresholdPx / DAMPING
    }

    fun isIdle(nowMs: Long): Boolean = releasesOnIdle && distance > 0f && nowMs - lastDeltaMs >= idleMillis
}