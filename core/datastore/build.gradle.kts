plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.koinCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.datastore.preferences)
            api(libs.okio)
            implementation(projects.core.common)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}