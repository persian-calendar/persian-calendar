package com.byagowi.persiancalendar.ui.utils

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.byagowi.persiancalendar.CALENDAR_READ_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.LOCATION_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.ui.DrawerHost
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
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

fun FragmentActivity.bringMarketPage() = runCatching {
    startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
}.onFailure(logException).onFailure {
    runCatching {
        val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }.onFailure(logException)
}.let {}

fun Bitmap.toPngBase64(): String {
    val buffer = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, buffer)
    val base64 = Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT)
    return "data:image/png;base64,$base64"
}

private fun Context.saveTextAsFile(text: String, fileName: String) = FileProvider.getUriForFile(
    applicationContext, "$packageName.provider",
    File(externalCacheDir, fileName).also { it.writeText(text) }
)

fun Context.openHtmlInBrowser(html: String) = runCatching {
    CustomTabsIntent.Builder().build()
        .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        .launchUrl(this, saveTextAsFile(html, "persian-calendar.html"))
}.onFailure(logException).let {}

fun FragmentActivity.shareText(text: String) = runCatching {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(getString(R.string.date_converter))
        .setText(text)
        .startChooser()
}.onFailure(logException).let {}

fun FragmentActivity.shareTextFile(text: String, fileName: String, mime: String) = runCatching {
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).also {
        it.type = mime
        it.putExtra(Intent.EXTRA_STREAM, saveTextAsFile(text, fileName))
    }, getString(R.string.share)))
}.onFailure(logException).let {}

fun Toolbar.setupUpNavigation() {
    navigationIcon = DrawerArrowDrawable(context).also { it.progress = 1f }
    setNavigationContentDescription(androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description)
    setNavigationOnClickListener { findNavController().navigateUp() }
}

fun Toolbar.setupMenuNavigation() {
    (context.getActivity() as? DrawerHost)?.setupToolbarWithDrawer(this)
}

// https://stackoverflow.com/a/58249983
private tailrec fun Context.getActivity(): FragmentActivity? = this as? FragmentActivity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()

@ColorInt
fun Context.resolveColor(@AttrRes attribute: Int) = TypedValue().let {
    theme.resolveAttribute(attribute, it, true)
    ContextCompat.getColor(this, it.resourceId)
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

fun Context.getAnimatedDrawable(@DrawableRes animatedDrawableRes: Int) =
    AnimatedVectorDrawableCompat.create(this, animatedDrawableRes)

// https://stackoverflow.com/a/48421144 but doesn't seem to be needed anymore?
fun AppBarLayout.hideToolbarBottomShadow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) outlineProvider = null
}

fun View.fadeIn(durationMillis: Long = 250) {
    this.startAnimation(AlphaAnimation(0F, 1F).also {
        it.duration = durationMillis
        it.fillAfter = true
    })
}

inline fun MenuItem.onClick(crossinline action: () -> Unit) =
    this.setOnMenuItemClickListener { action(); false /* let it handle selected menu */ }.let {}

fun View.setupExpandableAccessibilityDescription() {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.addAction(
                AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, resources.getString(R.string.more)
                )
            )
        }
    })
}

fun ViewGroup.setupLayoutTransition() {
    this.layoutTransition = LayoutTransition().also {
        it.enableTransitionType(LayoutTransition.CHANGING)
        it.setAnimateParentHierarchy(false) // this essentially was important to prevent rare crashes
    }
}

fun FragmentActivity.askForLocationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    MaterialAlertDialogBuilder(this)
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

fun FragmentActivity.askForCalendarPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    MaterialAlertDialogBuilder(this)
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
fun FragmentActivity.askForPostNotificationPermission(requestCode: Int) {
    requestPermissions(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        requestCode
    )
}

fun Window.makeWallpaperTransparency() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        this.navigationBarColor = Color.TRANSPARENT
    this.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
    this.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
}

class SystemBarsTransparency(activity: Activity) {
    val isPrimaryColorLight = ColorUtils.calculateLuminance(
        activity.resolveColor(R.attr.colorOnAppBar)
    ) < 0.5
    val isSurfaceColorLight = ColorUtils.calculateLuminance(
        activity.resolveColor(com.google.android.material.R.attr.colorSurface)
    ) > 0.5
    val needsVisibleStatusBarPlaceHolder = !isPrimaryColorLight && isSurfaceColorLight

    private val isLightStatusBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    private val isLightNavigationBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    // Either primary color, what we use behind above status icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightStatusBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldStatusBarBeTransparent = !isPrimaryColorLight || isLightStatusBarAvailable

    // Either surface color, what we use behind below navigation icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightNavigationBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldNavigationBarBeTransparent = !isSurfaceColorLight || isLightNavigationBarAvailable
}

/**
 * Make system bars (status and navigation bars) transparent as far as possible, also disables
 * decor view insets so we should consider the insets ourselves.
 *
 * From https://stackoverflow.com/a/76018821 with some modifications
 * Also have a look at [com.google.android.material.internal.EdgeToEdgeUtils.applyEdgeToEdge]
 */
fun Activity.transparentSystemBars() {
    // Android 4 is hard to debug and apparently ViewCompat.setOnApplyWindowInsetsListener isn't
    // reporting any value there so let's skip and simplify
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

    WindowCompat.setDecorFitsSystemWindows(window, false)

    val transparencyState = SystemBarsTransparency(this)

    if (transparencyState.isPrimaryColorLight || transparencyState.isSurfaceColorLight) {
        val insetsController: WindowInsetsControllerCompat =
            WindowCompat.getInsetsController(window, window.decorView)
        if (transparencyState.isPrimaryColorLight)
            insetsController.isAppearanceLightStatusBars = true
        if (transparencyState.isSurfaceColorLight)
            insetsController.isAppearanceLightNavigationBars = true
    }

    val systemUiScrim = ColorUtils.setAlphaComponent(Color.BLACK, 0x40) // 25% black
    window.statusBarColor =
        if (transparencyState.shouldStatusBarBeTransparent) Color.TRANSPARENT else systemUiScrim
    window.navigationBarColor =
        if (transparencyState.shouldNavigationBarBeTransparent) Color.TRANSPARENT else systemUiScrim
}

fun Snackbar.considerSystemBarsInsets() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return // no insets tweak here either
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return // not needed in 30 >=
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        // Not the best way but setOnApplyWindowInsetsListener refuses to give the value
        bottomMargin = (48 + 8).dp.toInt()
    }
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
            e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ) = callback(velocityX, velocityY)
    }

    return GestureDetector(context, FlingListener())
}
