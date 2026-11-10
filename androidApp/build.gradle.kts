plugins {
    alias(libs.plugins.wallify.android.application)
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(libs.koin.android)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
    // Coil finds its network fetcher at runtime; it loads images through Ktor, which needs an engine.
    implementation(libs.coil.network.ktor)
    implementation(libs.ktor.client.okhttp)
}