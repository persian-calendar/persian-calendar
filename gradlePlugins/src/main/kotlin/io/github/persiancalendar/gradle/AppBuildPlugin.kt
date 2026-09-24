package io.github.persiancalendar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class AppBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("codegenerators", CodeGenerators::class.java) {
            execute(target)
        }
        target.tasks.register("wearcodegenerators", CodeGenerators::class.java) {
            execute(target, true)
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
