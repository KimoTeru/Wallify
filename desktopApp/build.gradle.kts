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
}