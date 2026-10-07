package io.github.persiancalendar.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File

internal fun generatedAppSourceDir(project: Project): Provider<Directory> =
    project.layout.buildDirectory.dir("generated/source/appsrc/main")

internal fun ExecOperations.git(dir: File?, vararg args: String): String {
    val out = ByteArrayOutputStream()
    this.exec {
        if (dir != null) workingDir(dir)
        commandLine(listOf("git") + args)
        standardOutput = out
        errorOutput = ByteArrayOutputStream()
        isIgnoreExitValue = true
    }
    return out.toString().trimEnd()
}
