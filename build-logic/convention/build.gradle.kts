plugins {
    `kotlin-dsl`
}

group = "org.kimoteru.wallify.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "wallify.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("compose") {
            id = "wallify.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("apiConfig") {
            id = "wallify.api.config"
            implementationClass = "ApiConfigConventionPlugin"
        }
        register("feature") {
            id = "wallify.feature"
            implementationClass = "FeatureConventionPlugin"
        }
        register("androidApplication") {
            id = "wallify.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("desktopApplication") {
            id = "wallify.desktop.application"
            implementationClass = "DesktopApplicationConventionPlugin"
        }
    }
}