plugins {
    alias(libs.plugins.wallify.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.splash)
            implementation(projects.feature.auth)
            implementation(projects.feature.photo)
            implementation(projects.feature.profile)
            implementation(projects.feature.main)
        }
    }
}