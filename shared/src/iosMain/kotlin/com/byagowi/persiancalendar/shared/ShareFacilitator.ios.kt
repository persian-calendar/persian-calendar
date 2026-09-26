@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.byagowi.persiancalendar.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ShareFacilitator private actual constructor() {
    actual companion object {
        @Composable
        actual fun create(chooserTitle: String): ShareFacilitator {
            val currentViewController by rememberUpdatedState(LocalUIViewController.current)
            return object : ShareFacilitator() {
                override val viewController: UIViewController get() = currentViewController
            }
        }
    }

    protected abstract val viewController: UIViewController

    actual fun shareImageBitmap(imageBitmap: ImageBitmap) {
        val bytes = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        val image = UIImage(data = bytes.toNSData()) ?: return
        present(listOf(image))
    }

    actual fun shareText(text: String) = present(listOf(text))

    actual fun shareTextFile(text: String, fileName: String, mime: String) {
        present(listOf(writeToTempFile(fileName, text.encodeToByteArray())))
    }

    actual fun openHtmlInBrowser(html: String) {
        present(listOf(writeToTempFile("persian-calendar.html", html.encodeToByteArray())))
    }

    private fun present(items: List<Any>) {
        val activity = UIActivityViewController(activityItems = items, applicationActivities = null)
        val view = viewController.view
        if (view != null) {
            val popover = activity.popoverPresentationController
            if (popover != null) {
                popover.sourceView = view
                popover.sourceRect = view.bounds
            }
        }
        viewController.presentViewController(activity, animated = true, completion = null)
    }

    private fun writeToTempFile(fileName: String, bytes: ByteArray): NSURL {
        val path = NSTemporaryDirectory() + fileName
        bytes.toNSData().writeToFile(path, atomically = true)
        return NSURL.fileURLWithPath(path)
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}
