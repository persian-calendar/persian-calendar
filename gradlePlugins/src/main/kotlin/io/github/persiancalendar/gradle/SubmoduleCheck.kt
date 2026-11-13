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

        // Source archives (as used by some distribution builders) have no git metadata at all.
        if (gitResult(rootDir, "rev-parse", "--git-dir").exitCode != 0) {
            logger.lifecycle("Submodule check skipped: not a git checkout.")
            return
        }

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

                // Shallow checkouts (actions/checkout with fetch-depth: 1, F-Droid's clone)
                // cannot prove ancestry: the recorded commit may be an ancestor of the remote
                // branch while the shallow boundary hides the link. This guard is a release aid,
                // not a build requirement, so only verify when that is actually possible.
                if (git(subDir, "rev-parse", "--is-shallow-repository").trim() == "true") {
                    logger.lifecycle(
                        "Submodule check: skipping remote verification for $path (shallow checkout).",
                    )
                    return@forEach
                }

                // Refresh remote-tracking refs first, since `git push` does not update them.
                // Offline builds (F-Droid) cannot fetch; do not turn that into a failure either.
                if (gitResult(subDir, "fetch", "--quiet", "origin").exitCode != 0) {
                    logger.lifecycle(
                        "Submodule check: skipping remote verification for $path (could not fetch origin).",
                    )
                    return@forEach
                }

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

    private fun gitResult(dir: File, vararg args: String): GitResult =
        execOperations.gitResult(dir, *args)
}
