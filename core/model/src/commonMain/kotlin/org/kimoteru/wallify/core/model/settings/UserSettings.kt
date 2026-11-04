package org.kimoteru.wallify.core.model.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    /** Language of the app and of photo search, one of [org.kimoteru.wallify.core.model.settings.SupportedLanguages]. */
    @SerialName("language_tag")
    val languageTag: String = SupportedLanguages.first().tag,

    @SerialName("theme_mode")
    val themeMode: ThemeMode = ThemeMode.System,

    @SerialName("photo_layout")
    val photoLayout: PhotoLayout = PhotoLayout.Masonry,

    /** Columns of the photo masonry and grid; `null` lets the window width decide. */
    @SerialName("photo_columns")
    val photoColumns: Int? = null,

    @SerialName("screen_layout")
    val screenLayout: ScreenLayout = ScreenLayout.Auto
) {
    companion object {
        val Default: UserSettings = UserSettings()
    }
}

/** Color scheme of the app: [System] follows the device, [Night] and [Day] are chosen by the user. */
@Serializable
enum class ThemeMode { System, Night, Day }

/**
 * Arrangement of the whole screen: [Auto] follows the window width, the others force the phone ([Compact]),
 * tablet ([Medium]) or desktop ([Expanded]) arrangement whatever the window size is.
 */
@Serializable
enum class ScreenLayout { Auto, Compact, Medium, Expanded }

/**
 * Default layout of photo lists: [Masonry] keeps the proportions of the photos, [Grid] makes every card the same
 * size, [List] shows large cards in one column.
 */
@Serializable
enum class PhotoLayout { Masonry, Grid, List }