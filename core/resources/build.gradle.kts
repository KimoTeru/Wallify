plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.wallify.compose)
}

// The only place with strings, fonts and images: other modules read them through this public `Res`.
compose.resources {
    publicResClass = true
}

kotlin {
    android {
        // Without this the Android-KMP library plugin does not package Compose resources into the app:
        // the app crashes with MissingResourceException on the first string.
        androidResources.enable = true
    }
}