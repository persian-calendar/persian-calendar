package com.byagowi.persiancalendar.shared

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.byagowi.persiancalendar.utils.logException
import java.io.ByteArrayOutputStream
import java.io.File

actual abstract class ShareFacilitator private actual constructor() {
    actual companion object {
        @Composable
        actual fun create(chooserTitle: String): ShareFacilitator {
            val context by rememberUpdatedState(LocalContext.current)
            val chooserTitle by rememberUpdatedState(chooserTitle)
            return object : ShareFacilitator() {
                override val context: Context get() = context
                override val chooserTitle: String get() = chooserTitle
            }
        }
    }

    protected abstract val context: Context
    protected abstract val chooserTitle: String

    actual fun shareImageBitmap(imageBitmap: ImageBitmap) {
        context.shareBinaryFile(
            binary = imageBitmap.asAndroidBitmap().toPngByteArray(),
            fileName = "result.png",
            mime = "image/png",
        )
    }

    actual fun shareText(text: String) = context.shareText(text)

    actual fun shareTextFile(text: String, fileName: String, mime: String): Unit =
        context.shareTextFile(text, fileName, mime)

    actual fun openHtmlInBrowser(html: String) {
        runCatching {
            CustomTabsIntent.Builder().build()
                .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }.launchUrl(
                    context,
                    context.saveAsCacheFile("persian-calendar.html") { it.writeText(html) },
                )
        }.onFailure(logException)
    }

    private inline fun Context.saveAsCacheFile(
        fileName: String,
        crossinline action: (File) -> Unit,
    ): Uri {
        return FileProvider.getUriForFile(
            applicationContext,
            "$packageName.provider",
            File(externalCacheDir, fileName).also(action),
        )
    }

    // fun Bitmap.toPngBase64(): String =
    //     "data:image/png;base64," + Base64.encodeToString(toByteArray(), Base64.DEFAULT)

    private fun Bitmap.toPngByteArray(): ByteArray {
        val buffer = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, buffer)
        return buffer.toByteArray()
    }

    private fun Context.shareText(text: String) {
        runCatching {
            ShareCompat.IntentBuilder(this).setType("text/plain").setChooserTitle(chooserTitle)
                .setText(text).startChooser()
        }.onFailure(logException)
    }

    private fun Context.shareUriFile(uri: Uri, mime: String) {
        runCatching {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).also {
                        it.type = mime
                        it.putExtra(Intent.EXTRA_STREAM, uri)
                    },
                    chooserTitle,
                ),
            )
        }.onFailure(logException)
    }

    private fun Context.shareTextFile(text: String, fileName: String, mime: String) =
        shareUriFile(saveAsCacheFile(fileName) { it.writeText(text) }, mime)

    private fun Context.shareBinaryFile(binary: ByteArray, fileName: String, mime: String) =
        shareUriFile(saveAsCacheFile(fileName) { it.writeBytes(binary) }, mime)
}
