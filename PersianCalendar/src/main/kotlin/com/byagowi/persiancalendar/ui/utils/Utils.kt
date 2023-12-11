package com.byagowi.persiancalendar.ui.utils

import android.Manifest
import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.byagowi.persiancalendar.CALENDAR_READ_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import java.io.ByteArrayOutputStream
import java.io.File

inline val Resources.isRtl get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL || language.isLessKnownRtl
inline val Resources.isPortrait get() = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
inline val Resources.isLandscape get() = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
inline val Resources.dp: Float get() = displayMetrics.density
fun Resources.sp(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, displayMetrics)

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

fun Context?.copyToClipboard(text: CharSequence?) {
    runCatching {
        this?.getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(null, text)) ?: return@runCatching null
        if (Build.VERSION.SDK_INT < 32) {
            val message = (if (resources.isRtl) RLM else "") +
                    getString(R.string.date_copied_clipboard, text)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else Unit
    }.onFailure(logException).getOrNull().debugAssertNotNull
}

fun ComponentActivity.bringMarketPage() {
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

fun Bitmap.toPngBase64(): String =
    "data:image/png;base64," + Base64.encodeToString(toByteArray(), Base64.DEFAULT)

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

fun ComponentActivity.shareText(text: String) {
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
 * Returns the color int for the provided theme color attribute
 *
 * Source: https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/color/MaterialColors.java#L92
 */
@ColorInt
fun Context.resolveColor(@AttrRes attributeResId: Int): Int {
    return ContextCompat.getColor(this, resolveResourceIdFromTheme(attributeResId))
}

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

fun NavController.navigateSafe(directions: NavDirections) {
    runCatching { navigate(directions) }.onFailure(logException).getOrNull().debugAssertNotNull
}

fun Context?.getCompatDrawable(@DrawableRes drawableRes: Int): Drawable {
    return this?.let { AppCompatResources.getDrawable(it, drawableRes) }.debugAssertNotNull
        ?: ShapeDrawable()
}

inline fun MenuItem.onClick(crossinline action: () -> Unit) {
    this.setOnMenuItemClickListener { action(); false /* let it handle selected menu */ }
}

fun ViewGroup.setupLayoutTransition() {
    this.layoutTransition = LayoutTransition().also {
        it.enableTransitionType(LayoutTransition.CHANGING)
        it.setAnimateParentHierarchy(false) // this essentially was important to prevent rare crashes
    }
}

fun ComponentActivity.askForCalendarPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    // Maybe use ActivityCompat.shouldShowRequestPermissionRationale here? But in my testing it
    // didn't go well in Android 6.0 so better not risk I guess
    AlertDialog.Builder(this)
        .setTitle(R.string.calendar_access)
        .setMessage(R.string.phone_calendar_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_READ_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun ComponentActivity.askForPostNotificationPermission(requestCode: Int) {
    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCode)
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

fun View.performHapticFeedbackLongPress() {
    debugLog("Preformed a haptic feedback long press")
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}

/**
 * Determines if a color should be considered light or dark.
 *
 * Source: https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/color/MaterialColors.java#L252
 */
fun isColorLight(@ColorInt color: Int): Boolean =
    color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5
