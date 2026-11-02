package org.kimoteru.wallify.core.model.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    @SerialName("language_tag")
    val languageTag: String? = null
) {
    companion object {
        val Default: UserSettings = UserSettings()
    }
}