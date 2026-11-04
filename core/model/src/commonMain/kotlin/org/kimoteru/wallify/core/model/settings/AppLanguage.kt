package org.kimoteru.wallify.core.model.settings

/**
 * A language the app offers. [tag] is the language code (what `UserSettings.languageTag` holds and the photo service
 * takes), [name] is how the language is called in itself, so a user finds it whatever the current language is.
 */
data class AppLanguage(
    val tag: String,
    val name: String
)

/** Languages of the app. Each one needs a `values-<tag>/strings.xml` in `core:resources` (`code/resources.md`). */
val SupportedLanguages: List<AppLanguage> = listOf(
    AppLanguage("en", "English"),
    AppLanguage("ru", "Русский")
)