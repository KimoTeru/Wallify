package org.kimoteru.wallify.buildlogic.plugin

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
 * The keys are required: `generateApiConfig` fails without them.
 */
class ApiConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val generateApiConfig = tasks.register<GenerateApiConfigTask>("generateApiConfig") {
            accessKey.set(localSecret("api.accessKey", "API_ACCESS_KEY"))
            secretKey.set(localSecret("api.secretKey", "API_SECRET_KEY"))
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