import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libsPlugin("kotlinMultiplatform"))
        pluginManager.apply(libsPlugin("androidMultiplatformLibrary"))

        extensions.configure<KotlinMultiplatformExtension> {
            jvm {
                compilerOptions.jvmTarget.set(ProjectConfig.jvmTarget)
            }
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = androidNamespace()
                compileSdk = ProjectConfig.COMPILE_SDK
                minSdk = ProjectConfig.MIN_SDK
                compilerOptions.jvmTarget.set(ProjectConfig.jvmTarget)
            }
            sourceSets.named("commonTest").configure {
                dependencies {
                    implementation(library("kotlin-test"))
                }
            }
        }
    }
}