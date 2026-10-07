package io.github.persiancalendar.gradle

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class GitInfoValueSource : ValueSource<String, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String = listOf(
        git("rev-parse", "--abbrev-ref", "HEAD"), // branch, e.g. main
        git("rev-list", "HEAD", "--count"), // number of commits in history, e.g. 3724
        git("rev-parse", "--short", "HEAD"), // git hash, e.g. 2426d51f
        git("status", "-s").let { if (it.isEmpty()) "" else "dirty" }, // -dirty if is uncommited
    ).filter { it.isNotEmpty() }.joinToString("-")

    private fun git(vararg args: String): String = execOperations.git(null, *args)
}
