package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.MorphedPath
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout

// It is somehow a permanent sandbox to test views not used yet elsewhere, it can be removed anytime.
fun showHiddenDialog(activity: FragmentActivity) {
    val root = LinearLayout(activity)
    root.orientation = LinearLayout.VERTICAL
    root.addView(
        TabLayout(
            activity, null, R.style.TabLayoutColored
        ).also { tabLayout ->
            val tintColor = activity.resolveColor(R.attr.normalTabTextColor)
            listOf(
                R.drawable.ic_developer to -1,
                R.drawable.ic_translator to 0,
                R.drawable.ic_motorcycle to 1,
                R.drawable.ic_help to 33,
                R.drawable.ic_bug to 9999
            ).map { (iconId: Int, badgeNumber: Int) ->
                tabLayout.addTab(tabLayout.newTab().also { tab ->
                    tab.setIcon(iconId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tab.icon?.setTint(tintColor)
                    }
                    tab.orCreateBadge.also { badge ->
                        badge.isVisible = badgeNumber >= 0
                        if (badgeNumber > 0) badge.number = badgeNumber
                    }
                })
            }
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.orCreateBadge?.isVisible = false
                }
            })
            tabLayout.setSelectedTabIndicator(R.drawable.cat_tabs_pill_indicator)
            tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_STRETCH)
        })
    root.addView(LinearProgressIndicator(activity).also { indicator ->
        indicator.isIndeterminate = true
        indicator.setIndicatorColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)
        indicator.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    })

    val morphedPathView = object : View(activity) {
        private val pathMorph = MorphedPath(
            "m 100 0 l -100 100 l 100 100 l 100 -100 z",
            "m 50 50 l 0 100 l 100 0 l 0 -100 z"
        )
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.BLACK
        }

        init {
            val scale = 100.dp.toInt()
            layoutParams = LinearLayout.LayoutParams(scale, scale).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawPath(pathMorph.path, paint)
        }

        fun setFraction(value: Float) {
            pathMorph.interpolateTo(value)
            invalidate()
        }
    }
    root.addView(morphedPathView)
    root.addView(Slider(activity).also {
        it.addOnChangeListener { _, value, _ -> morphedPathView.setFraction(value) }
    })

    root.addView(ProgressBar(activity).also { progressBar ->
        progressBar.isIndeterminate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ValueAnimator.ofArgb(
            Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
        ).also { valueAnimator ->
            valueAnimator.duration = 3000
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.repeatMode = ValueAnimator.REVERSE
            valueAnimator.repeatCount = 1
            valueAnimator.addUpdateListener {
                progressBar.indeterminateDrawable?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        it.animatedValue as Int, BlendModeCompat.SRC_ATOP
                    )
            }
        }.start()
        progressBar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
    })

    BottomSheetDialog(activity).also { it.setContentView(root) }.show()
}
