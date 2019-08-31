package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.CalendarFragmentDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.ui.shared.SimpleDayPickerView
import dagger.android.support.DaggerAppCompatDialogFragment
import javax.inject.Inject

/**
 * Created by ebrahim on 3/20/16.
 */
class SelectDayDialog : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    @Inject
    lateinit var calendarFragmentDependency: CalendarFragmentDependency

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val jdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L

        val mainActivity = mainActivityDependency.mainActivity
        val dayPickerView = SimpleDayPickerView(mainActivity)
        dayPickerView.setDayJdnOnView(jdn)

        return AlertDialog.Builder(mainActivity)
                .setView(dayPickerView as View)
                .setCustomTitle(null)
                .setPositiveButton(R.string.go) { _, _ ->
                    val resultJdn = dayPickerView.dayJdnFromView
                    if (resultJdn != -1L)
                        calendarFragmentDependency.calendarFragment.bringDate(resultJdn)
                }.create()
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = SelectDayDialog().apply {
            arguments = Bundle().apply {
                putLong(BUNDLE_KEY, jdn)
            }
        }
    }
}
