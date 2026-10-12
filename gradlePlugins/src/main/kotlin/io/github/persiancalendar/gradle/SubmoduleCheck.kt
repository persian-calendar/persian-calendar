package io.github.persiancalendar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class SubmoduleCheck : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val layout: ProjectLayout

    @TaskAction
    fun run() {
        val rootDir = layout.projectDirectory.asFile

        val failures = buildList {
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
                    'U' -> {
                        add("$path is in a conflicted state")
                        return@forEach
                    }
                    // '+' means the checkout differs from the parent's recorded commit.
                    // That's fine: only the recorded commit matters for a fresh clone,
                    // and a submodule remote that has moved ahead is harmless.
                }

                val subDir = File(rootDir, path)

                if (git(subDir, "status", "--porcelain").isNotBlank()) {
                    add("submodule has uncommitted changes: $path")
                }

                // Refresh remote-tracking refs first, since `git push` does not update them.
                git(subDir, "fetch", "--quiet", "origin")

                // Local checkout ahead of the remote → unpushed commits.
                // (Behind the remote is fine: HEAD is still contained in a remote branch.)
                if (git(subDir, "branch", "-r", "--contains", "HEAD").isBlank()) {
                    add("submodule HEAD ($sha) is not pushed to its remote: $path")
                }

                // The commit recorded by the parent repo is what a fresh clone (e.g. F-Droid)
                // will check out, so it must exist on the submodule's remote.
                val recordedSha = git(rootDir, "ls-tree", "HEAD", "--", path)
                    .trim()
                    .substringBefore('\t')
                    .substringAfterLast(' ')

                if (recordedSha.isNotEmpty() &&
                    git(subDir, "branch", "-r", "--contains", recordedSha).isBlank()
                ) {
                    add(
                        "submodule commit $recordedSha recorded by the parent repo is not " +
                                "available on its remote: $path"
                    )
                }
            }
        }

        if (failures.isNotEmpty()) throw GradleException(
            "Submodule check failed — commit and push submodule changes before building a release.\n" +
                    failures.joinToString("\n") { "  - $it" },
        )
        logger.lifecycle("Submodule check passed.")
    }

    private fun git(dir: File, vararg args: String): String = execOperations.git(dir, *args)
}
