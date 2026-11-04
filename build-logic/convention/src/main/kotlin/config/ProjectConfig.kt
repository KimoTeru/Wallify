package org.kimoteru.wallify.buildlogic.config

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/** Constants shared by all convention plugins. */
internal object ProjectConfig {
    const val APPLICATION_ID = "org.kimoteru.wallify"
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    const val COMPILE_SDK = 37
    const val MIN_SDK = 24
    const val TARGET_SDK = 37
    const val DESKTOP_MAIN_CLASS = "org.kimoteru.wallify.MainKt"

    val javaVersion: JavaVersion = JavaVersion.VERSION_11
    val jvmTarget: JvmTarget = JvmTarget.JVM_11
}

/** Ids of the convention plugins registered in `convention/build.gradle.kts`. */
internal object PluginIds {
    const val KMP_LIBRARY = "wallify.kmp.library"
    const val COMPOSE = "wallify.compose"
}