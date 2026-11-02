import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
                        packageName = ProjectConfig.APPLICATION_ID
                        packageVersion = ProjectConfig.VERSION_NAME
                    }
                }
            }
        }

        val composeDependencies = extensions.getByType(ComposeExtension::class.java).dependencies
        dependencies {
            implementation(composeDependencies.desktop.currentOs)
            implementation(library("kotlinx-coroutinesSwing"))
        }
    }
}