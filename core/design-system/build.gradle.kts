plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.wallify.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            api(libs.androidx.paging.compose)
            api(libs.compose.material3.adaptive)
            api(libs.compose.material3.adaptive.layout)
            api(libs.compose.material3.adaptive.navigation)
            api(libs.compose.material3.adaptive.navigationSuite)
            api(libs.haze)
            implementation(libs.haze.blur)
        }
    }
}