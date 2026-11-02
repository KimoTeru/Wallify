import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Gives a KMP module the backend address and key as generated constants (`ApiBuildConfig`) in `commonMain`.
 * Values come from `local.properties` or the environment, see `docs/setup/config-and-secrets.md`.
 */
class ApiConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val generateApiConfig = tasks.register<GenerateApiConfigTask>("generateApiConfig") {
            baseUrl.set(localSecret("api.baseUrl", "API_BASE_URL", ProjectConfig.DEFAULT_API_BASE_URL))
            accessKey.set(localSecret("api.accessKey", "API_ACCESS_KEY"))
            enableLogging.set(localSecret("api.enableLogging", "API_ENABLE_LOGGING", "false").toBoolean())
            outputDir.set(layout.buildDirectory.dir("generated/apiConfig/kotlin"))
        }

        pluginManager.withPlugin(libsPlugin("kotlinMultiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain").configure {
                    kotlin.srcDir(generateApiConfig.flatMap { it.outputDir })
                }
            }
        }
    }
}