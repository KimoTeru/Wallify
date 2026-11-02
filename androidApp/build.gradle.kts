plugins {
    alias(libs.plugins.wallify.android.application)
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(libs.koin.android)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
}