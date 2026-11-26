package org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh

/**
 * Whether a pull of the list is let go when no new scroll delta arrives for a moment. Desktop wheel and trackpad
 * scrolling have no release; on Android the end of the touch gesture releases, and a finger held still must not.
 */
internal expect val isPullReleasedOnIdle: Boolean