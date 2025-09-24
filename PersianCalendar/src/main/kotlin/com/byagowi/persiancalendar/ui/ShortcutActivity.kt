package com.byagowi.persiancalendar.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.byagowi.persiancalendar.R

// It should be in sync with res/xml/shortcuts.xml
abstract class Shortcut(
    private val action: String,
    @get:StringRes private val stringId: Int,
    @get:DrawableRes private val icon: Int
) : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = getString(stringId)
        val shortcut = ShortcutInfoCompat.Builder(this, action)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIntent(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setAction(action)
            )
            .setIcon(IconCompat.createWithResource(this, icon))
            .build()
        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))
        finish()
    }
}

class ConverterShortcutActivity : Shortcut("CONVERTER", R.string.converter, R.drawable.sc_converter)
class CompassShortcutActivity : Shortcut("COMPASS", R.string.compass, R.drawable.sc_compass)
class LevelShortcutActivity : Shortcut("LEVEL", R.string.level, R.drawable.sc_level)
class AstronomyShortcutActivity : Shortcut("ASTRONOMY", R.string.horoscope, R.drawable.sc_astronomy)
class MapShortcutActivity : Shortcut("MAP", R.string.map, R.drawable.sc_map)
