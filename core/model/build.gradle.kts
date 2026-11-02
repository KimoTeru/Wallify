plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization)
        }
    }
}