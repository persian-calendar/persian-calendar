package com.byagowi.persiancalendar.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect abstract class ShareFacilitator private expect constructor() {
    companion object {
        @Composable
        fun create(chooserTitle: String): ShareFacilitator
    }

    fun shareImageBitmap(imageBitmap: ImageBitmap)
    fun shareText(text: String)
    fun shareTextFile(text: String, fileName: String, mime: String)
    fun openHtmlInBrowser(html: String)
}
