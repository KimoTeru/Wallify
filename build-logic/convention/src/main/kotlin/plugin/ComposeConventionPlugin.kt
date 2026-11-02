package org.kimoteru.wallify.buildlogic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.kimoteru.wallify.buildlogic.extension.implementation
import org.kimoteru.wallify.buildlogic.extension.library
import org.kimoteru.wallify.buildlogic.extension.libsPlugin

private val composeLibraries = listOf(
    "compose-runtime",
    "compose-foundation",
    "compose-material3",
    "compose-ui",
    "compose-components-resources",
    "compose-uiToolingPreview"
)

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libsPlugin("composeMultiplatform"))
        pluginManager.apply(libsPlugin("composeCompiler"))

        pluginManager.withPlugin(libsPlugin("kotlinMultiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain").configure {
                    dependencies {
                        composeLibraries.forEach { implementation(library(it)) }
                    }
                }
            }
        }

        pluginManager.withPlugin(libsPlugin("kotlinJvm")) {
            dependencies {
                composeLibraries.forEach { implementation(library(it)) }
            }
        }
    }
}