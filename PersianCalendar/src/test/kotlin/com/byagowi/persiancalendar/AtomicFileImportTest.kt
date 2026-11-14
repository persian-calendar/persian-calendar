package com.byagowi.persiancalendar

import android.annotation.SuppressLint
import com.byagowi.persiancalendar.utils.importFileAtomically
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// These tests run on the host JVM; exceptions deliberately simulate provider failures.
@SuppressLint("NewApi", "ThrowUsage", "TryCatchUsage")
class AtomicFileImportTest {
    private val replace: (File, File) -> Unit = { source, target ->
        Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    @Test
    fun `failed reads oversized files invalid validations and failed replacements preserve the previous file`() {
        val directory = Files.createTempDirectory("font-import").toFile()
        try {
            val destination = File(directory, "font.ttf").apply { writeText("old") }
            val broken = object : InputStream() {
                override fun read(): Int = throw IOException("provider failed")
            }
            assertFailsWith<IOException> { importFileAtomically(broken, destination, 10, replace) { true }.getOrThrow() }
            assertFailsWith<IOException> {
                importFileAtomically("too large".byteInputStream(), destination, 2, replace) { true }.getOrThrow()
            }
            assertFailsWith<IOException> {
                importFileAtomically("invalid".byteInputStream(), destination, 10, replace) { error ->
                    error.readText() == "valid"
                }.getOrThrow()
            }
            assertFailsWith<IOException> {
                importFileAtomically("valid".byteInputStream(), destination, 10,
                    replace = { _, _ -> throw IOException("rename failed") }) { true }.getOrThrow()
            }
            assertEquals("old", destination.readText())
            assertEquals(listOf("font.ttf"), directory.listFiles().orEmpty().map { it.name })
            importFileAtomically("valid".byteInputStream(), destination, 10, replace) { true }.getOrThrow()
            assertEquals("valid", destination.readText())
        } finally {
            directory.deleteRecursively()
        }
    }
}
