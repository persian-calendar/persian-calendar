package io.github.persiancalendar.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AppBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val generatedSourceDir = target.layout.buildDirectory.dir("generated/source/appsrc/main")

        target.tasks.register("codegenerators", CodeGenerators::class.java) {
            getGeneratedAppSrcDir().set(generatedSourceDir)
            getIsWear().set(false)
            configure()
        }
        target.tasks.register("wearcodegenerators", CodeGenerators::class.java) {
            getGeneratedAppSrcDir().set(generatedSourceDir)
            getIsWear().set(true)
            configure()
        }

        // Register the generated sources with AGP so they are compiled and linted.
        target.plugins.withId("com.android.application") {
            val android = target.extensions.getByType(CommonExtension::class.java)
            android.sourceSets.getByName("main").kotlin.directories += generatedSourceDir.get().asFile.path
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
