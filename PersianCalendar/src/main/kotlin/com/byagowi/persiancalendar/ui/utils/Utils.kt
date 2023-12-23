package com.byagowi.persiancalendar.ui.utils

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import java.io.ByteArrayOutputStream
import java.io.File

inline val Resources.isRtl get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL || language.value.isLessKnownRtl
inline val Resources.isPortrait get() = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
inline val Resources.dp: Float get() = displayMetrics.density
fun Resources.sp(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, displayMetrics)

fun Context.bringMarketPage() {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    }.onFailure(logException).onFailure {
        runCatching {
            val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure(logException)
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val buffer = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, buffer)
    return buffer.toByteArray()
}

//fun Bitmap.toPngBase64(): String =
//    "data:image/png;base64," + Base64.encodeToString(toByteArray(), Base64.DEFAULT)

private inline fun Context.saveAsFile(fileName: String, crossinline action: (File) -> Unit): Uri {
    return FileProvider.getUriForFile(
        applicationContext, "$packageName.provider",
        File(externalCacheDir, fileName).also(action)
    )
}

fun Context.openHtmlInBrowser(html: String) {
    runCatching {
        CustomTabsIntent.Builder().build()
            .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            .launchUrl(this, saveAsFile("persian-calendar.html") { it.writeText(html) })
    }.onFailure(logException)
}

fun Context.shareText(text: String) {
    runCatching {
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle(getString(R.string.date_converter))
            .setText(text)
            .startChooser()
    }.onFailure(logException)
}

private fun Context.shareUriFile(uri: Uri, mime: String) {
    runCatching {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).also {
            it.type = mime
            it.putExtra(Intent.EXTRA_STREAM, uri)
        }, getString(R.string.share)))
    }.onFailure(logException)
}

fun Context.shareTextFile(text: String, fileName: String, mime: String) =
    shareUriFile(saveAsFile(fileName) { it.writeText(text) }, mime)

fun Context.shareBinaryFile(binary: ByteArray, fileName: String, mime: String) =
    shareUriFile(saveAsFile(fileName) { it.writeBytes(binary) }, mime)

// https://stackoverflow.com/a/58249983
// Akin to https://github.com/material-components/material-components-android/blob/8938da8c/lib/java/com/google/android/material/internal/ContextUtils.java#L40
tailrec fun Context.getActivity(): ComponentActivity? = this as? ComponentActivity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()

/**
 * Turns an attribute to a resource id from the theme
 *
 * Source: https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/resources/MaterialAttributes.java#L45
 */
@AnyRes
fun Context.resolveResourceIdFromTheme(@AttrRes attributeId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return typedValue.resourceId
}

// Return an empty drawable instead of crash, to be removed someday hopefully
fun Context.getSafeDrawable(@DrawableRes drawableRes: Int): Drawable =
    runCatching { getDrawable(drawableRes) }.onFailure(logException).getOrNull().debugAssertNotNull
        ?: ColorDrawable(Color.TRANSPARENT)

inline fun MenuItem.onClick(crossinline action: () -> Unit) {
    this.setOnMenuItemClickListener { action(); false /* let it handle selected menu */ }
}

@Composable
fun AskForCalendarPermissionDialog(setGranted: (Boolean) -> Unit) {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return setGranted(true)
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) }
        return setGranted(true)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, isGranted) }
        updateStoredPreference(context)
        setGranted(isGranted)
    }

    // Maybe use ActivityCompat.shouldShowRequestPermissionRationale here? But in my testing it
    // didn't go well in Android 6.0 so better not risk I guess

    var showDialog by rememberSaveable { mutableStateOf(true) }
    if (showDialog) AlertDialog(
        title = { Text(stringResource(R.string.calendar_access)) },
        confirmButton = {
            TextButton(onClick = {
                showDialog = false
                launcher.launch(Manifest.permission.READ_CALENDAR)
            }) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = {
                context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                setGranted(false)
            }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { setGranted(false) },
        text = { Text(stringResource(R.string.phone_calendar_required)) },
    )
}

fun Window.makeWallpaperTransparency() {
    this.navigationBarColor = Color.TRANSPARENT
    this.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
    this.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
}

fun prepareViewForRendering(view: View, width: Int, height: Int) {
    view.layoutDirection = view.context.resources.configuration.layoutDirection
    // https://stackoverflow.com/a/69080742
    view.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
    )
    view.layout(0, 0, width, height)
}

fun createFlingDetector(
    context: Context, callback: (velocityX: Float, velocityY: Float) -> Boolean
): GestureDetector {
    class FlingListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean = callback(velocityX, velocityY)
    }

    return GestureDetector(context, FlingListener())
}

/**
 * Similar to [androidx.compose.foundation.isSystemInDarkTheme] implementation but
 * for non composable contexts, in composable context, use the compose one.
 */
fun isSystemInDarkTheme(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

// Android 14 will have a grayscale dynamic colors mode and this is somehow a hack to check for that
// I guess there will be better ways to check for that in the future I guess but this does the trick
// Android 13, at least in Extension 5 emulator image, also provides such theme.
// https://stackoverflow.com/a/76272434
val Context.isDynamicGrayscale: Boolean
    get() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val hsv = FloatArray(3)
        return listOf(
            android.R.color.system_accent1_500,
            android.R.color.system_accent2_500,
            android.R.color.system_accent3_500,
        ).all { Color.colorToHSV(getColor(it), hsv); hsv[1] < .25 }
    }

fun View.performHapticFeedbackVirtualKey() {
    debugLog("Preformed a haptic feedback virtual key")
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}
