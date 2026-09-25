package io.github.persiancalendar.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal fun generatedAppSourceDir(project: Project): Provider<Directory> =
    project.layout.buildDirectory.dir("generated/source/appsrc/main")

class AppBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Codegen runs only in the module(s) that bundle the source data
        // (currently the shared KMP module).
        if (target.file("data/events/events.json").exists()) {
            val taskProvider = target.tasks.register("codegenerators", CodeGenerators::class.java) {
                configure()
            }

            target.plugins.withId("com.android.application") {
                val androidComponents =
                    target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
                androidComponents.onVariants { variant ->
                    variant.sources.kotlin?.addGeneratedSourceDirectory(
                        taskProvider,
                        CodeGenerators::getGeneratedAppSrcDir,
                    )
                }
            }
        }

        target.tasks.register("updateDependenciesReport", DependenciesReport::class.java) {
            configurationName.set("releaseRuntimeClasspath")
            reportFile.set(target.layout.projectDirectory.file("runtime-dependencies-report.txt"))
        }

        // guard against duplicate task registration (wear, the main app)
        val root = target.rootProject
        val checkSubmodules = root.tasks.findByName("checkSubmodules")
            ?: root.tasks.register("checkSubmodules", SubmoduleCheck::class.java)
        target.tasks.matching { it.name == "preReleaseBuild" || it.name == "preNightlyBuild" }.configureEach {
            dependsOn(checkSubmodules)
        }
    }
}
