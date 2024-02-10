package io.github.persiancalendar.gradle

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class GitInfoValueSource : ValueSource<String, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String = listOf(
        "git rev-parse --abbrev-ref HEAD", // branch, e.g. main
        "git rev-list HEAD --count", // number of commits in history, e.g. 3724
        "git rev-parse --short HEAD", // git hash, e.g. 2426d51f
        "git status -s" // i == 3, whether repo's dir is clean, -dirty is appended if smt is uncommitted
    ).mapIndexedNotNull { i, cmd ->
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(cmd.split(" "))
            standardOutput = output
        }
        output.toString().trim().takeIf { it.isNotEmpty() }
            ?.let { if (i == 3) "dirty" else it }
    }.joinToString("-")
}
