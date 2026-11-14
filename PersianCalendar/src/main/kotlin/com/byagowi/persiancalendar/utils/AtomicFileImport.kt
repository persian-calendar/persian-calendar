package com.byagowi.persiancalendar.utils

import java.io.File
import java.io.IOException
import java.io.InputStream

/** Stages and validates an import before replacing the last working file. */
internal fun importFileAtomically(
    input: InputStream,
    destination: File,
    maxBytes: Long,
    replace: (File, File) -> Unit,
    validate: (File) -> Boolean,
): Result<Unit> {
    val temporary = runCatching {
        File.createTempFile("import-", ".tmp", destination.parentFile)
    }.getOrElse { return Result.failure(it) }
    val result = runCatching {
        temporary.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxBytes) return@runCatching Result.failure<Unit>(
                    IOException("Import exceeds size limit"),
                )
                output.write(buffer, 0, count)
            }
            output.fd.sync()
        }
        if (!validate(temporary)) return@runCatching Result.failure<Unit>(
            IOException("Import failed validation"),
        )
        // Both files are in the same directory. Never delete the destination first:
        // if replacement fails, the previously selected file must survive.
        replace(temporary, destination)
        Result.success(Unit)
    }.getOrElse { Result.failure(it) }
    temporary.delete()
    return result
}
