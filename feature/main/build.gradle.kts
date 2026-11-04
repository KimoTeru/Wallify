plugins {
    alias(libs.plugins.wallify.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.feed)
            api(projects.feature.topics)
            api(projects.feature.random)
            api(projects.feature.profile)
            api(projects.feature.settings)
        }
    }
}