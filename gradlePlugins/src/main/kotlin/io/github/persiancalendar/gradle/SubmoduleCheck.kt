package io.github.persiancalendar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

abstract class SubmoduleCheck : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val layout: ProjectLayout

    @TaskAction
    fun run() {
        val failures = mutableListOf<String>()
        val rootDir = layout.projectDirectory.asFile

        git(
            rootDir,
            "submodule", "status", "--recursive",
        ).lineSequence().filter { it.isNotBlank() }.forEach { line ->
            val prefix = line[0]
            val rest = line.drop(1).trimStart()
            val sha = rest.substringBefore(' ')
            val path = rest.substringAfter(' ').substringBefore(" (")

            when (prefix) {
                '-' -> return@forEach // uninitialized submodule — nothing checked out to verify
                '+' -> {
                    failures += "$path is checked out at $sha but the parent repo records a different commit (run: git add $path)"
                    return@forEach
                }

                'U' -> {
                    failures += "$path is in a conflicted state"
                    return@forEach
                }
            }

            val subDir = File(rootDir, path)

            if (git(subDir, "status", "--porcelain").isNotBlank()) {
                failures += "submodule has uncommitted changes: $path"
            }

            // Refresh remote-tracking refs first, since `git push` does not update them.
            git(subDir, "fetch", "--quiet", "origin")
            if (git(subDir, "branch", "-r", "--contains", "HEAD").isBlank()) {
                failures += "submodule HEAD ($sha) is not pushed to its remote: $path"
            }
        }

        if (failures.isNotEmpty()) {
            throw GradleException(
                "Submodule check failed — commit and push submodule changes before building a release.\n" +
                    failures.joinToString("\n") { "  - $it" },
            )
        }
        logger.lifecycle("Submodule check passed.")
    }

    private fun git(dir: File, vararg args: String): String {
        val out = ByteArrayOutputStream()
        execOperations.exec {
            workingDir(dir)
            commandLine(listOf("git") + args)
            standardOutput = out
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        return out.toString().trimEnd()
    }
}
