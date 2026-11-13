package io.github.persiancalendar.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File

internal fun generatedAppSourceDir(project: Project): Provider<Directory> =
    project.layout.buildDirectory.dir("generated/source/appsrc/main")

internal data class GitResult(val output: String, val exitCode: Int)

internal fun ExecOperations.gitResult(dir: File?, vararg args: String): GitResult {
    val out = ByteArrayOutputStream()
    val result = this.exec {
        if (dir != null) workingDir(dir)
        commandLine(listOf("git") + args)
        standardOutput = out
        errorOutput = ByteArrayOutputStream()
        isIgnoreExitValue = true
    }
    return GitResult(out.toString().trimEnd(), result.exitValue)
}

internal fun ExecOperations.git(dir: File?, vararg args: String): String =
    gitResult(dir, *args).output
