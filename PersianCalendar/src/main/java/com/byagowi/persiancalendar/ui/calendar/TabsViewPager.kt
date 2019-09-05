package com.byagowi.persiancalendar.ui.calendar

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.ui.calendar.times.SunView
import com.duolingo.open.rtlviewpager.RtlViewPager

// https://stackoverflow.com/a/47774679
class TabsViewPager : RtlViewPager {
    private var mCurrentView: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec: Int = heightMeasureSpec
        if (mCurrentView != null) {
            var height = 0
            mCurrentView?.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val h = mCurrentView?.measuredHeight
            if (h != null && h > height) height = h
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun measureCurrentView(currentView: View?) {
        mCurrentView = currentView
        requestLayout()
    }

    class TabsAdapter internal constructor(fm: FragmentManager, private val mAppDependency: AppDependency, private val mTabs: List<View>, private val mTitles: List<String>) : FragmentStatePagerAdapter(fm) {
        private var mCurrentPosition = -1

        override fun getPageTitle(position: Int): CharSequence? {
            return mTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            return TabFragment.newInstance(mTabs[position])
        }

        override fun getCount(): Int {
            return mTabs.size
        }

        // https://stackoverflow.com/a/47774679
        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)

            if (position != mCurrentPosition && container is TabsViewPager) {
                val fragment = `object` as Fragment

                if (fragment.view != null) {
                    val tab = fragment.view
                    container.measureCurrentView(tab)

                    if (mTabs.size > 2) {
                        val sunView = mTabs[Constants.OWGHAT_TAB].findViewById<View>(R.id.sunView)
                        if (sunView is SunView) {
                            if (position == Constants.OWGHAT_TAB) {
                                sunView.startAnimate(false)
                            } else {
                                sunView.clear()
                            }
                        }
                    }

                    mCurrentPosition = position
                }
            }

            val editor = mAppDependency.sharedPreferences.edit()
            editor.putInt(Constants.LAST_CHOSEN_TAB_KEY, position)
            editor.apply()
        }

        // don't remove public ever
        class TabFragment : Fragment() {
            @get:JvmName("getView_")
            lateinit var view: View

            override fun onCreateView(inflater: LayoutInflater,
                                      container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return view
            }

            companion object {
                internal fun newInstance(view: View): TabFragment {
                    val tabFragment = TabFragment()
                    tabFragment.view = view
                    return tabFragment
                }
            }
        }
    }
}
