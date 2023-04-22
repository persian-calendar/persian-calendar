package io.github.persiancalendar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.File


class AppBuildPlugin : Plugin<Project> {

    private operator fun File.div(child: String) = File(this, child)

    override fun apply(target: Project) {
        target.tasks.register("codegenerators", CodeGenerators::class.java) {
            val task = this
            val projectDir = project.projectDir
            val generatedAppSrcDir = target.buildDir / "generated" / "source" / "appsrc" / "main"
            generatedAppSrcDir.mkdirs()
            task.setProperty("generatedAppSrcDir", generatedAppSrcDir)
            val generateDir =
                generatedAppSrcDir / "com" / "byagowi" / "persiancalendar" / "generated"

            val actions = listOf("events", "cities", "districts")
            actions.forEach { name ->
                val input = projectDir / "data" / "$name.json"
                inputs.file(input)
                val output = generateDir / "${name.capitalized()}.kt"
                outputs.file(output)
            }
            inputs.file(project.rootDir / "THANKS.md")
            inputs.file(project.rootDir / "FAQ.fa.md")
            inputs.file(projectDir / "shaders" / "common.vert")
            inputs.file(projectDir / "shaders" / "globe.frag")
            inputs.file(projectDir / "shaders" / "sandbox.frag")
            outputs.file(generateDir / "TextStore.kt")
        }
    }
}
