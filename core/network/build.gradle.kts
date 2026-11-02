plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.wallify.api.config)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koinCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}