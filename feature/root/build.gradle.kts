plugins {
    alias(libs.plugins.wallify.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.splash)
            implementation(projects.feature.home)
        }
    }
}