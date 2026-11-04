package org.kimoteru.wallify

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.awt.GraphicsEnvironment
import java.util.prefs.Preferences

private const val KEY_WIDTH = "window_width"
private const val KEY_HEIGHT = "window_height"
private const val KEY_X = "window_x"
private const val KEY_Y = "window_y"
private const val KEY_MAXIMIZED = "window_maximized"

private const val DEFAULT_WIDTH = 1100f
private const val DEFAULT_HEIGHT = 800f

/** Window geometry to open the window with. */
internal data class StoredWindow(
    val placement: WindowPlacement,
    val position: WindowPosition,
    val size: DpSize
)

/**
 * Keeps the size, position and maximized state of the desktop window between launches, so the app opens the way
 * it was closed. It is state of the platform window, not a user setting, so it lives here and not in the settings.
 * While the window is maximized the last normal size and position stay stored: they are what un-maximizing returns to.
 */
internal class WindowStateStore {

    private val preferences = Preferences.userNodeForPackage(WindowStateStore::class.java)

    /** [minSize] is the smallest window allowed: a stored size below it (or a broken one) is replaced by the default. */
    fun restore(minSize: DpSize): StoredWindow {
        val width = preferences.getFloat(KEY_WIDTH, DEFAULT_WIDTH)
        val height = preferences.getFloat(KEY_HEIGHT, DEFAULT_HEIGHT)
        val x = preferences.getFloat(KEY_X, Float.NaN)
        val y = preferences.getFloat(KEY_Y, Float.NaN)

        val size = if (width >= minSize.width.value && height >= minSize.height.value) {
            DpSize(width.dp, height.dp)
        } else {
            DpSize(DEFAULT_WIDTH.dp, DEFAULT_HEIGHT.dp)
        }

        // A monitor can be gone since the last launch: a position outside every screen is dropped.
        val position = if (!x.isNaN() && !y.isNaN() && isOnScreen(x, y)) {
            WindowPosition(x.dp, y.dp)
        } else {
            WindowPosition.PlatformDefault
        }

        val placement = if (preferences.getBoolean(KEY_MAXIMIZED, false)) {
            WindowPlacement.Maximized
        } else {
            WindowPlacement.Floating
        }

        return StoredWindow(placement = placement, position = position, size = size)
    }

    fun save(state: WindowState) {
        preferences.putBoolean(KEY_MAXIMIZED, state.placement == WindowPlacement.Maximized)

        if (state.placement != WindowPlacement.Floating) return

        if (state.size.isSpecified) {
            preferences.putFloat(KEY_WIDTH, state.size.width.value)
            preferences.putFloat(KEY_HEIGHT, state.size.height.value)
        }

        if (state.position.isSpecified) {
            preferences.putFloat(KEY_X, state.position.x.value)
            preferences.putFloat(KEY_Y, state.position.y.value)
        }

        preferences.flush()
    }

    private fun isOnScreen(x: Float, y: Float): Boolean =
        GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.any { screen ->
            screen.defaultConfiguration.bounds.contains(x.toInt(), y.toInt())
        }
}