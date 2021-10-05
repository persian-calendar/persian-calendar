package com.byagowi.persiancalendar.ui.utils

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.byagowi.persiancalendar.CALENDAR_READ_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.LOCATION_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.ui.DrawerHost
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.appbar.AppBarLayout
import java.io.File

val Number.dp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.density
val Number.sp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

fun Context?.copyToClipboard(
    text: CharSequence?,
    onSuccess: ((String) -> Unit) = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
) = runCatching {
    this?.getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText(null, text)) ?: return@runCatching null
    val message = (if (resources.isRtl) RLM else "") +
            getString(R.string.date_copied_clipboard, text)
    onSuccess(message)
}.onFailure(logException).getOrNull().debugAssertNotNull.let {}

fun Activity.bringMarketPage() = runCatching {
    startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
}.onFailure(logException).onFailure {
    runCatching {
        val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }.onFailure(logException)
}.let {}

fun Context.showHtml(html: String) = runCatching {
    val uri = FileProvider.getUriForFile(
        applicationContext, "$packageName.provider",
        File(externalCacheDir, "temp.html").also { it.writeText(html) }
    )
    CustomTabsIntent.Builder().build()
        .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        .launchUrl(this, uri)
}.onFailure(logException).let {}

fun Activity.shareText(text: String, fileName: String, mime: String) = runCatching {
    val uri = FileProvider.getUriForFile(
        applicationContext, "$packageName.provider",
        File(externalCacheDir, fileName).also { it.writeText(text) }
    )
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).also {
        it.type = mime
        it.putExtra(Intent.EXTRA_STREAM, uri)
    }, getString(R.string.share)))
}.onFailure(logException).let {}

fun Toolbar.setupUpNavigation() {
    navigationIcon = DrawerArrowDrawable(context).also { it.progress = 1f }
    setNavigationContentDescription(androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description)
    setNavigationOnClickListener { findNavController().navigateUp() }
}

fun Toolbar.setupMenuNavigation() {
    (context.getActivity() as? DrawerHost).debugAssertNotNull?.setupToolbarWithDrawer(this)
}

// https://stackoverflow.com/a/58249983
private tailrec fun Context.getActivity(): Activity? = this as? Activity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()

@ColorInt
fun Context.resolveColor(attr: Int) = TypedValue().let {
    theme.resolveAttribute(attr, it, true)
    ContextCompat.getColor(this, it.resourceId)
}

val Context.isDarkTheme get() = TypedValue().let {
    theme.resolveAttribute(R.attr.isDarkTheme, it, false)
    it.data != 0 // https://stackoverflow.com/a/60667225
}

fun Flow.addViewsToFlow(viewList: List<View>) {
    val parentView = (this.parent as? ViewGroup).debugAssertNotNull ?: return
    this.referencedIds = viewList.map {
        View.generateViewId().also { id ->
            it.id = id
            parentView.addView(it)
        }
    }.toIntArray()
}

fun NavController.navigateSafe(directions: NavDirections) = runCatching {
    navigate(directions)
}.onFailure(logException).getOrNull().debugAssertNotNull.let {}

fun Context.getCompatDrawable(@DrawableRes drawableRes: Int) =
    AppCompatResources.getDrawable(this, drawableRes).debugAssertNotNull ?: ShapeDrawable()

fun AppBarLayout.hideToolbarBottomShadow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) outlineProvider = null
}

inline fun MenuItem.onClick(crossinline action: () -> Unit) =
    this.setOnMenuItemClickListener { action(); false /* let it handle selected menu */ }.let {}

fun View.setupExpandableAccessibilityDescription() {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View?,
            info: AccessibilityNodeInfoCompat?
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info?.addAction(
                AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, resources.getString(R.string.more)
                )
            )
        }
    })
}

fun Activity.askForLocationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    AlertDialog.Builder(this)
        .setTitle(R.string.location_access)
        .setMessage(R.string.phone_location_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

fun Activity.askForCalendarPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
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

fun Window.makeStatusBarTransparent() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
    // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
    this.attributes = this.attributes.also {
        it.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
    }
    this.statusBarColor = Color.TRANSPARENT
}

fun Window.makeWallpaperTransparency() {
    makeStatusBarTransparent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        this.navigationBarColor = Color.TRANSPARENT
    this.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
    this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

// Whether we can enable the new interface
val canEnableNewInterface = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

// Make the new interface for Android 12 opt-out instead of opt-in
val shouldEnableNewInterface = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
