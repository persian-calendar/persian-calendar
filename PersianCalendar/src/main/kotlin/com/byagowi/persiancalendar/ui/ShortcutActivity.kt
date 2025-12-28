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
abstract class BaseShortcut(val shortcut: Shortcut) : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = getString(shortcut.stringId)
        val shortcut = ShortcutInfoCompat.Builder(this, shortcut.name)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIntent(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setAction(shortcut.name),
            )
            .setIcon(IconCompat.createWithResource(this, shortcut.icon))
            .build()
        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))
        finish()
    }
}

// It should match xml/shortcuts.xml also
enum class Shortcut(@get:StringRes val stringId: Int, @get:DrawableRes val icon: Int) {
    CONVERTER(R.string.converter, R.drawable.sc_converter),
    COMPASS(R.string.compass, R.drawable.sc_compass),
    LEVEL(R.string.level, R.drawable.sc_level),
    ASTRONOMY(R.string.horoscope, R.drawable.sc_astronomy),
    MAP(R.string.map, R.drawable.sc_map);

    companion object {
        fun fromName(name: String?) = Shortcut.entries.firstOrNull { it.name == name }
    }
}

class ConverterShortcutActivity : BaseShortcut(Shortcut.CONVERTER)
class CompassShortcutActivity : BaseShortcut(Shortcut.COMPASS)
class LevelShortcutActivity : BaseShortcut(Shortcut.LEVEL)
class AstronomyShortcutActivity : BaseShortcut(Shortcut.ASTRONOMY)
class MapShortcutActivity : BaseShortcut(Shortcut.MAP)
