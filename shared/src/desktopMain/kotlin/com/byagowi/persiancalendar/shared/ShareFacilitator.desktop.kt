package com.byagowi.persiancalendar.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.byagowi.persiancalendar.utils.logException
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ShareFacilitator {
    actual companion object {
        @Composable
        @Suppress("DEPRECATION")
        actual fun create(chooserTitle: String): ShareFacilitator {
            val clipboard by rememberUpdatedState(LocalClipboardManager.current)
            return object : ShareFacilitator() {
                override val clipboard: ClipboardManager get() = clipboard
            }
        }
    }

    @Suppress("DEPRECATION")
    protected abstract val clipboard: ClipboardManager

    actual fun shareImageBitmap(imageBitmap: ImageBitmap) {
        runCatching {
            val bytes = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
                .encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
            val file = saveAsTempFile("result.png").apply { writeBytes(bytes) }
            openFile(file)
        }.onFailure(logException)
    }

    actual fun shareText(text: String) {
        runCatching {
            @Suppress("DEPRECATION")
            clipboard.setText(AnnotatedString(text))
        }.onFailure(logException)
    }

    actual fun shareTextFile(text: String, fileName: String, mime: String) {
        runCatching {
            val file = saveAsTempFile(fileName).apply { writeText(text) }
            openFile(file)
        }.onFailure(logException)
    }

    actual fun openHtmlInBrowser(html: String) {
        runCatching {
            val file = saveAsTempFile("persian-calendar.html").apply { writeText(html) }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(file.toURI())
            }
        }.onFailure(logException)
    }

    private fun saveAsTempFile(fileName: String): File =
        File(System.getProperty("java.io.tmpdir"), fileName)

    private fun openFile(file: File) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }
}
