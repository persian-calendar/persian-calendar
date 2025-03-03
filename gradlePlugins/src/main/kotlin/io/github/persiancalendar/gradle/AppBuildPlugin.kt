package io.github.persiancalendar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project


class AppBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("codegenerators", CodeGenerators::class.java) {
            execute(target)
        }
        target.tasks.register("eventsgenerators", CodeGenerators::class.java) {
            execute(target, true)
        }
    }
}
