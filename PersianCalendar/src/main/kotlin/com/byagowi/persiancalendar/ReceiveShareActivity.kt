package com.byagowi.persiancalendar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.utils.saveAsFile
import com.byagowi.persiancalendar.utils.preferences

class ReceiveShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_SEND) IntentCompat.getParcelableExtra(
            intent, Intent.EXTRA_STREAM, Uri::class.java
        )?.also { sharedUri ->
            contentResolver.openInputStream(sharedUri)?.use { inputStream ->
                saveAsFile(STORED_FONT_NAME) { file ->
                    file.writeBytes(inputStream.readBytes())
                }
                preferences.edit { putBoolean(PREF_HAS_STORED_FONT, true) }
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        finish()
    }
}
