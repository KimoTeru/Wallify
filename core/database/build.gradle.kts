plugins {
    alias(libs.plugins.wallify.kmp.library)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.room.runtime)
            implementation(projects.core.common)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}