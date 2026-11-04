package org.kimoteru.wallify.buildlogic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.kimoteru.wallify.buildlogic.config.PluginIds
import org.kimoteru.wallify.buildlogic.config.ProjectConfig
import org.kimoteru.wallify.buildlogic.extension.implementation
import org.kimoteru.wallify.buildlogic.extension.library
import org.kimoteru.wallify.buildlogic.extension.libsPlugin
import org.kimoteru.wallify.buildlogic.graph.applyAppGraph

class DesktopApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libsPlugin("kotlinJvm"))
        pluginManager.apply(PluginIds.COMPOSE)
        applyAppGraph()

        extensions.configure<ComposeExtension>("compose") {
            (this as ExtensionAware).extensions.configure<DesktopExtension>("desktop") {
                application {
                    mainClass = ProjectConfig.DESKTOP_MAIN_CLASS
                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        // The sign-in redirect is caught by a local HTTP server (`JvmAuthBrowser`); the trimmed
                        // runtime of a packaged app does not include its module by default.
                        modules("jdk.httpserver")
                        packageName = ProjectConfig.APPLICATION_ID
                        packageVersion = ProjectConfig.VERSION_NAME
                    }
                }
            }
        }

        // Desktop has no build types: only the run tasks (`run`, `hotRun`) are debug, a packaged app is release.
        // The argument is added right before the start, so the task setup of the Compose plugin cannot drop it.
        tasks.withType(JavaExec::class.java).configureEach {
            doFirst { jvmArgs("-Dwallify.debug=true") }
        }

        val composeDependencies = extensions.getByType(ComposeExtension::class.java).dependencies
        dependencies {
            implementation(composeDependencies.desktop.currentOs)
            implementation(library("kotlinx-coroutinesSwing"))
        }
    }
}