@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

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
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsArray
import kotlin.js.toJsNumber
import kotlin.js.toJsString

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ShareFacilitator private actual constructor() {
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
        val uint8 = Uint8Array(bytes.map { (it.toInt() and 0xFF).toJsNumber() }.toJsArray())
        download(blob(uint8, "image/png"), "result.png")
    }

    actual fun shareText(text: String) {
        @Suppress("DEPRECATION")
        clipboard.setText(AnnotatedString(text))
    }

    actual fun shareTextFile(text: String, fileName: String, mime: String) {
        download(blob(text.toJsString(), mime), fileName)
    }

    actual fun openHtmlInBrowser(html: String) {
        val url = URL.createObjectURL(blob(html.toJsString(), "text/html"))
        window.open(url, "_blank")
        return
    }

    private fun blob(part: JsAny, type: String): Blob {
        val parts = JsArray<JsAny?>()
        parts[0] = part
        return Blob(parts, BlobPropertyBag(type))
    }

    private fun download(blob: Blob, fileName: String) {
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        window.setTimeout(
            {
                URL.revokeObjectURL(url)
                null
            },
            10_000,
        )
    }
}
