package org.kimoteru.wallify.core.presentation.settings

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.model.settings.ScreenLayout
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_auto
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_compact
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_expanded
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_medium

/** Label of a screen arrangement. [windowType] is null when the window width decides. */
enum class ScreenLayoutUi(
    internal val domain: ScreenLayout,
    val label: StringResource,
    val windowType: WindowType?,
) {
    Auto(ScreenLayout.Auto, Res.string.settings_screen_layout_auto, null),
    Compact(ScreenLayout.Compact, Res.string.settings_screen_layout_compact, WindowType.Compact),
    Medium(ScreenLayout.Medium, Res.string.settings_screen_layout_medium, WindowType.Medium),
    Expanded(ScreenLayout.Expanded, Res.string.settings_screen_layout_expanded, WindowType.Expanded),
}

fun ScreenLayoutUi.toDomain(): ScreenLayout = domain

fun ScreenLayout.toPresentation(): ScreenLayoutUi = ScreenLayoutUi.entries.first { it.domain == this }