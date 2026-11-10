plugins {
    alias(libs.plugins.wallify.desktop.application)
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(projects.core.resources)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
    implementation(libs.essenty.lifecycle)
    implementation(libs.essenty.lifecycle.coroutines)
    // Coil finds its network fetcher at runtime; it loads images through Ktor, which needs an engine.
    implementation(libs.coil.network.ktor)
    implementation(libs.ktor.client.okhttp)
}