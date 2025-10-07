package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_HAS_CUSTOM_FONT
import com.byagowi.persiancalendar.PREF_VAZIR_ENABLED
import com.byagowi.persiancalendar.STORED_FONT_NAME
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.preferences
import java.io.File

class ReceiveShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_SEND) IntentCompat.getParcelableExtra(
            intent, Intent.EXTRA_STREAM, Uri::class.java
        )?.also { sharedUri ->
            contentResolver.openInputStream(sharedUri)?.use { inputStream ->
                File(externalCacheDir, STORED_FONT_NAME)
                    .outputStream().use { inputStream.copyTo(it) }
                preferences.edit {
                    remove(PREF_VAZIR_ENABLED)
                    putBoolean(PREF_HAS_CUSTOM_FONT, true)
                }
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        finish()
    }
}
