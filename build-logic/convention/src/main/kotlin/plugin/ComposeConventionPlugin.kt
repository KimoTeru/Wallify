package org.kimoteru.wallify.buildlogic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.kimoteru.wallify.buildlogic.extension.androidNamespace
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

private fun ComposeExtension.resourcesExtension(): ResourcesExtension = (this as ExtensionAware).extensions.getByType<ResourcesExtension>()

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libsPlugin("composeMultiplatform"))
        pluginManager.apply(libsPlugin("composeCompiler"))

        // The module's `compose.resources { … }` block, if any, overrides this default.
        extensions.getByType<ComposeExtension>().resourcesExtension().packageOfResClass =
            "${androidNamespace()}.generated.resources"

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