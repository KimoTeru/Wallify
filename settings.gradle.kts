rootProject.name = "Wallify"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")

include(":core:common")
include(":core:platform")
include(":core:presentation")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:design-system")
include(":core:resources")
include(":core:database")
include(":core:datastore")
include(":core:cache")

include(":feature:root")
include(":feature:splash")
include(":feature:home")
include(":feature:auth")