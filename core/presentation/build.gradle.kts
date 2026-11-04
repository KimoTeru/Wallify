plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.wallify.compose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koinCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            api(projects.core.model)
            implementation(projects.core.platform)
            implementation(projects.core.designSystem)
            implementation(projects.core.resources)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.notifications)
            implementation(libs.moko.permissions.camera)
            implementation(libs.moko.permissions.gallery)
            implementation(libs.moko.permissions.storage)
        }
    }
}