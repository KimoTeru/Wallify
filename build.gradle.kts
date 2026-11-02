plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.koinCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}

tasks.register("allTests") {
    group = "verification"
    description = "Runs Kotlin tests in every module that defines them."
}

gradle.projectsEvaluated {
    tasks.named("allTests").configure {
        val testTasks = subprojects.mapNotNull { project ->
            project.tasks.findByName("allTests") ?: project.tasks.findByName("check")
        }
        setDependsOn(testTasks)
    }
}