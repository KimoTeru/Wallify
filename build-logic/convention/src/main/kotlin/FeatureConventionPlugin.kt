import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private val featureProjects = listOf(
    ":core:common",
    ":core:model",
    ":core:domain",
    ":core:design-system",
    ":core:presentation"
)

private val featureLibraries = listOf(
    "decompose",
    "decompose-compose",
    "essenty-lifecycle",
    "essenty-lifecycle-coroutines",
    "koin-core",
    "koin-annotations",
    "kotlinx-serialization"
)

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(PluginIds.KMP_LIBRARY)
        pluginManager.apply(PluginIds.COMPOSE)
        pluginManager.apply(libsPlugin("kotlinSerialization"))
        pluginManager.apply(libsPlugin("koinCompiler"))

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.named("commonMain").configure {
                dependencies {
                    featureProjects.forEach { implementation(project(it)) }
                    featureLibraries.forEach { implementation(library(it)) }
                }
            }
        }
    }
}