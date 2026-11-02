plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.koinCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}