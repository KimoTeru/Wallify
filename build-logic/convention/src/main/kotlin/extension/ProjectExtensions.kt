package org.kimoteru.wallify.buildlogic.extension

import java.io.StringReader
import java.util.Properties
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.kimoteru.wallify.buildlogic.config.ProjectConfig

internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.libsPlugin(name: String): String =
    catalog.findPlugin(name).get().get().pluginId

internal fun Project.library(alias: String): MinimalExternalModuleDependency =
    catalog.findLibrary(alias).get().get()

/** Android namespace from the project path: `:core:design-system` -> `org.kimoteru.wallify.core.designsystem`. */
internal fun Project.androidNamespace(): String =
    path.split(':')
        .filter { it.isNotEmpty() }
        .joinToString(separator = ".", prefix = "${ProjectConfig.APPLICATION_ID}.") { it.replace("-", "") }

/**
 * Reads a local value from `local.properties` (key [propertyName]) or from the environment ([envName]).
 * Returns [default] when neither is set, so builds still work without credentials.
 * Both sources are read through Gradle providers, so configuration cache sees them as build inputs.
 */
internal fun Project.localSecret(propertyName: String, envName: String, default: String = ""): String {
    val localProperties = providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .orNull

    val fromFile = localProperties
        ?.let { text -> Properties().apply { load(StringReader(text)) } }
        ?.getProperty(propertyName)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    val fromEnvironment = providers.environmentVariable(envName).orNull
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    return fromFile ?: fromEnvironment ?: default
}