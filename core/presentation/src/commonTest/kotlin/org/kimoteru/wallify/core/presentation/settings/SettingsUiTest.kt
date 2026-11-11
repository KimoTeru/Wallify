package org.kimoteru.wallify.core.presentation.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.settings.PhotoLayout
import org.kimoteru.wallify.core.model.settings.ScreenLayout
import org.kimoteru.wallify.core.model.settings.ThemeMode

/**
 * Every domain value has its own screen value and back. The mappers find the value by `domain` instead of a `when`,
 * so a value missing in `XxxUi` fails here, not on the screen.
 */
class SettingsUiTest {

    @Test
    fun everyThemeModeMapsBothWays() {
        ThemeMode.entries.forEach { mode -> assertEquals(mode, mode.toPresentation().toDomain()) }
        ThemeModeUi.entries.forEach { mode -> assertEquals(mode, mode.toDomain().toPresentation()) }
    }

    @Test
    fun everyImageQualityMapsBothWays() {
        ImageQuality.entries.forEach { quality -> assertEquals(quality, quality.toPresentation().toDomain()) }
        ImageQualityUi.entries.forEach { quality -> assertEquals(quality, quality.toDomain().toPresentation()) }
    }

    @Test
    fun everyPhotoLayoutMapsBothWays() {
        PhotoLayout.entries.forEach { layout -> assertEquals(layout, layout.toPresentation().toDomain()) }
        PhotoLayoutUi.entries.forEach { layout -> assertEquals(layout, layout.toDomain().toPresentation()) }
    }

    @Test
    fun everyScreenLayoutMapsBothWays() {
        ScreenLayout.entries.forEach { layout -> assertEquals(layout, layout.toPresentation().toDomain()) }
        ScreenLayoutUi.entries.forEach { layout -> assertEquals(layout, layout.toDomain().toPresentation()) }
    }
}