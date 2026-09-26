package com.byagowi.persiancalendar.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ShareFacilitator {
    actual companion object {
        @Composable
        actual fun create(chooserTitle: String): ShareFacilitator {
            TODO("Not yet implemented")
        }
    }

    actual fun shareImageBitmap(imageBitmap: ImageBitmap) {
    }

    actual fun shareText(text: String) {
    }

    actual fun shareTextFile(text: String, fileName: String, mime: String) {
    }

    actual fun openHtmlInBrowser(html: String) {
    }
}
