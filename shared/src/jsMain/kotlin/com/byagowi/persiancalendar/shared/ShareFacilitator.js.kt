package com.byagowi.persiancalendar.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

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
        val bytes = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        download(Blob(arrayOf(bytes), BlobPropertyBag("image/png")), "result.png")
    }

    actual fun shareText(text: String) {
        @Suppress("DEPRECATION")
        clipboard.setText(AnnotatedString(text))
    }

    actual fun shareTextFile(text: String, fileName: String, mime: String) {
        download(Blob(arrayOf(text), BlobPropertyBag(mime)), fileName)
    }

    actual fun openHtmlInBrowser(html: String) {
        val url = URL.createObjectURL(Blob(arrayOf(html), BlobPropertyBag("text/html")))
        window.open(url, "_blank")
    }

    private fun download(blob: Blob, fileName: String) {
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        window.setTimeout({ URL.revokeObjectURL(url) }, 10_000)
    }
}
