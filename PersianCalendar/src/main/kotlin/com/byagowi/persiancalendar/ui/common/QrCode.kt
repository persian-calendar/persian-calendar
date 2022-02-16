package com.byagowi.persiancalendar.ui.common

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.core.graphics.set
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

private fun textToQrCodeBitmap(text: String): Bitmap {
    val size = 768
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val bitMatrix = QRCodeWriter().encode(
        text, BarcodeFormat.QR_CODE, size, size, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0
        )
    )
    (0 until bitMatrix.height).forEach { y ->
        (0 until bitMatrix.width).forEach { x ->
            bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT
        }
    }
    return bitmap
}

fun showQrCode(activity: Activity, text: String) {
    MaterialAlertDialogBuilder(activity)
        .setView(ImageView(activity).also { it.setImageBitmap(textToQrCodeBitmap(text)) })
        .show()
}
