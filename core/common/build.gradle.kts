plugins {
    alias(libs.plugins.wallify.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.paging.common)
            implementation(libs.kotlinx.serialization)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}