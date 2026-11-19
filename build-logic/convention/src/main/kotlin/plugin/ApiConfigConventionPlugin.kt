package org.kimoteru.wallify.buildlogic.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.kimoteru.wallify.buildlogic.extension.libsPlugin
import org.kimoteru.wallify.buildlogic.extension.localSecret
import org.kimoteru.wallify.buildlogic.task.GenerateApiConfigTask

/**
 * Gives a KMP module the backend keys as generated constants (`ApiBuildConfig`) in `commonMain`.
 * Values come from `local.properties` or the environment, see `docs/setup/config-and-secrets.md`.
 * The keys are required and checked while the project is configured, so Gradle Sync fails without them.
 */
class ApiConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val accessKey = localSecret(ACCESS_KEY_NAME)
        val secretKey = localSecret(SECRET_KEY_NAME)
        checkRequiredKeys(ACCESS_KEY_NAME to accessKey, SECRET_KEY_NAME to secretKey)

        val generateApiConfig = tasks.register<GenerateApiConfigTask>("generateApiConfig") {
            this.accessKey.set(accessKey)
            this.secretKey.set(secretKey)
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

    private fun checkRequiredKeys(vararg keys: Pair<String, String>) {
        val missing = keys.filter { (_, value) -> value.isBlank() }.map { (name, _) -> name }
        if (missing.isEmpty()) return
        val example = missing.joinToString(separator = "\n") { "    $it=<your value>" }
        throw GradleException(
            """
            |Missing required backend keys: ${missing.joinToString()}.
            |The app cannot reach the backend without them, so the project does not sync or build.
            |Add them to local.properties in the project root:
            |$example
            |or set environment variables with the same names, then sync again.
            |See docs/setup/config-and-secrets.md.
            """.trimMargin()
        )
    }

    private companion object {
        const val ACCESS_KEY_NAME = "API_ACCESS_KEY"
        const val SECRET_KEY_NAME = "API_SECRET_KEY"
    }
}