package org.kimoteru.wallify.buildlogic.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.kimoteru.wallify.buildlogic.config.ProjectConfig
import org.kimoteru.wallify.buildlogic.extension.implementation
import org.kimoteru.wallify.buildlogic.extension.library
import org.kimoteru.wallify.buildlogic.extension.libsPlugin
import org.kimoteru.wallify.buildlogic.graph.applyAppGraph

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libsPlugin("androidApplication"))
        pluginManager.apply(libsPlugin("composeCompiler"))
        applyAppGraph()

        extensions.configure<ApplicationExtension> {
            namespace = ProjectConfig.APPLICATION_ID
            compileSdk = ProjectConfig.COMPILE_SDK
            defaultConfig {
                applicationId = ProjectConfig.APPLICATION_ID
                minSdk = ProjectConfig.MIN_SDK
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = ProjectConfig.VERSION_CODE
                versionName = ProjectConfig.VERSION_NAME
            }
            compileOptions {
                sourceCompatibility = ProjectConfig.javaVersion
                targetCompatibility = ProjectConfig.javaVersion
            }
            buildFeatures.compose = true
            packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
            buildTypes.getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                signingConfig = signingConfigs.getByName("debug")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions.jvmTarget.set(ProjectConfig.jvmTarget)
        }

        // `buildRelease` (root) collects the release builds into `build/release/` as `Wallify-<version>.<ext>`.
        extensions.configure<ApplicationAndroidComponentsExtension> {
            onVariants(selector().withBuildType("release")) { variant ->
                // The APK artifact carries its producing task, so the copy needs no `dependsOn` and no output path.
                val apkDir = variant.artifacts.get(SingleArtifact.APK)
                tasks.register("copyReleaseApk", Copy::class.java) {
                    from(apkDir) { include("*.apk") }
                    into(rootProject.layout.buildDirectory.dir("release"))
                    rename { "${ProjectConfig.APP_NAME}-${ProjectConfig.VERSION_NAME}.apk" }
                }
            }
        }

        dependencies {
            implementation(library("androidx-activity-compose"))
            implementation(library("compose-uiToolingPreview"))
            add("debugImplementation", library("compose-uiTooling"))
        }
    }
}