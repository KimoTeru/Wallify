package org.kimoteru.wallify.buildlogic.plugin

import java.util.UUID
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Copy
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
                    // The `…Release…` tasks (`packageReleaseDmg`, `runRelease`) shrink and optimize with ProGuard.
                    // Names stay unobfuscated (the default): Ktor and Koin look types up by them.
                    buildTypes.release.proguard {
                        configurationFiles.from(layout.projectDirectory.file("proguard-rules.pro"))
                    }
                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        // The sign-in redirect is caught by a local HTTP server (`JvmAuthBrowser`); the trimmed
                        // runtime of a packaged app does not include its module by default.
                        modules("jdk.httpserver")
                        // Shown as the app name in Applications / Start menu; the id stays in `bundleID`.
                        packageName = ProjectConfig.APP_NAME
                        packageVersion = ProjectConfig.VERSION_NAME
                        macOS {
                            bundleID = ProjectConfig.APPLICATION_ID
                            iconFile.set(layout.projectDirectory.file("src/main/resources/icons/icon.icns"))
                        }
                        windows {
                            iconFile.set(layout.projectDirectory.file("src/main/resources/icons/icon.ico"))
                            // Without it the installer adds no Start menu entry.
                            menu = true
                            // Windows recognizes a newer MSI as an update of the app by it, so it is derived from the
                            // application id and stays the same across versions.
                            upgradeUuid = UUID.nameUUIDFromBytes(ProjectConfig.APPLICATION_ID.toByteArray()).toString()
                        }
                        linux {
                            iconFile.set(layout.projectDirectory.file("src/main/resources/icons/icon.png"))
                            // Adds the app to the applications' menu.
                            shortcut = true
                        }
                    }
                }
            }
        }

        // Desktop has no build types: only the run tasks (`run`, `hotRun`) are debug, a packaged app is release.
        // The argument is added right before the start, so the task setup of the Compose plugin cannot drop it.
        tasks.withType(JavaExec::class.java).configureEach {
            doFirst { jvmArgs("-Dwallify.debug=true") }
        }

        // `buildRelease` (root) collects the release builds into `build/release/` as `Wallify-<version>.<ext>`.
        // Only the current version is taken: installers of older versions may still lie in the same folder.
        tasks.register("copyReleaseInstaller", Copy::class.java) {
            dependsOn("packageReleaseDistributionForCurrentOS")
            val version = ProjectConfig.VERSION_NAME
            from(layout.buildDirectory.dir("compose/binaries/main-release")) {
                include("**/*$version*.dmg", "**/*$version*.msi", "**/*$version*.deb")
            }
            into(rootProject.layout.buildDirectory.dir("release"))
            includeEmptyDirs = false
            eachFile {
                relativePath = RelativePath(true, "${ProjectConfig.APP_NAME}-$version.${file.extension}")
            }
        }

        val composeDependencies = extensions.getByType(ComposeExtension::class.java).dependencies
        dependencies {
            implementation(composeDependencies.desktop.currentOs)
            implementation(library("kotlinx-coroutinesSwing"))
        }
    }
}