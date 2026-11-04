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
    val label: StringResource,
    val windowType: WindowType?
) {
    Auto(Res.string.settings_screen_layout_auto, null),
    Compact(Res.string.settings_screen_layout_compact, WindowType.Compact),
    Medium(Res.string.settings_screen_layout_medium, WindowType.Medium),
    Expanded(Res.string.settings_screen_layout_expanded, WindowType.Expanded)
}

fun ScreenLayoutUi.toDomain(): ScreenLayout = when (this) {
    ScreenLayoutUi.Auto -> ScreenLayout.Auto
    ScreenLayoutUi.Compact -> ScreenLayout.Compact
    ScreenLayoutUi.Medium -> ScreenLayout.Medium
    ScreenLayoutUi.Expanded -> ScreenLayout.Expanded
}

fun ScreenLayout.toPresentation(): ScreenLayoutUi = when (this) {
    ScreenLayout.Auto -> ScreenLayoutUi.Auto
    ScreenLayout.Compact -> ScreenLayoutUi.Compact
    ScreenLayout.Medium -> ScreenLayoutUi.Medium
    ScreenLayout.Expanded -> ScreenLayoutUi.Expanded
}