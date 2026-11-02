plugins {
    alias(libs.plugins.wallify.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.common)
            api(libs.kotlinx.coroutines.core)
        }
    }
}