package org.kimoteru.wallify.buildlogic.graph

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.kimoteru.wallify.buildlogic.extension.implementation
import org.kimoteru.wallify.buildlogic.extension.library
import org.kimoteru.wallify.buildlogic.extension.libsPlugin

/**
 * Gradle modules that declare Koin modules. The compiler plugin discovers the `@Module @Configuration` classes only
 * in the dependencies of the module that holds `@KoinApplication`, so every application depends on all of them.
 * A new module with bindings is added here.
 */
private val graphProjects = listOf(
    ":core:platform",
    ":core:network",
    ":core:cache",
    ":core:database",
    ":core:datastore",
    ":core:data",
    ":core:presentation",
    ":feature:root",
    ":feature:main",
    ":feature:splash",
    ":feature:auth",
    ":feature:feed",
    ":feature:topics",
    ":feature:random",
    ":feature:photo",
    ":feature:profile",
    ":feature:settings"
)

/** Connects an application module to the whole dependency graph and enables Koin compile-time checks. */
internal fun Project.applyAppGraph() {
    pluginManager.apply(libsPlugin("koinCompiler"))

    dependencies {
        graphProjects.forEach { implementation(project(it)) }
        implementation(library("koin-core"))
        implementation(library("koin-annotations"))
    }
}