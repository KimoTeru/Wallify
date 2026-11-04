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
            implementation(libs.coil.network.ktor)
            api(libs.androidx.paging.compose)
            api(libs.compose.material3.adaptive)
            api(libs.compose.material3.adaptive.layout)
            api(libs.compose.material3.adaptive.navigation)
            api(libs.compose.material3.adaptive.navigationSuite)
            api(libs.haze)
            implementation(libs.haze.blur)
        }
        // Coil loads images through Ktor, which needs an engine on each platform.
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}