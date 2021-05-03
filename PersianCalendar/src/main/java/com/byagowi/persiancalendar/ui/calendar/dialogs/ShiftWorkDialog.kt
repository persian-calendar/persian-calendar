package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShiftWorkSettingsBinding
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentDirections
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getDateFromJdnOfCalendar
import com.byagowi.persiancalendar.utils.getTodayJdn
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.shiftWorkRecurs
import com.byagowi.persiancalendar.utils.shiftWorkStartingJdn
import com.byagowi.persiancalendar.utils.shiftWorkTitles
import com.byagowi.persiancalendar.utils.shiftWorks
import com.byagowi.persiancalendar.utils.spacedComma
import com.byagowi.persiancalendar.utils.updateStoredPreference

class ShiftWorkDialog : AppCompatDialogFragment() {

    private var jdn: Long = -1L
    private var selectedJdn: Long = -1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()

        applyAppLanguage(activity)
        updateStoredPreference(activity)

        selectedJdn = arguments?.getLong(BUNDLE_KEY, -1L)?.takeIf { it != -1L } ?: getTodayJdn()

        jdn = shiftWorkStartingJdn
        var isFirstSetup = false
        if (jdn == -1L) {
            isFirstSetup = true
            jdn = selectedJdn
        }

        val binding = ShiftWorkSettingsBinding.inflate(activity.layoutInflater, null, false)

        val state = mutableStateOf(
            if (shiftWorks.isEmpty()) listOf(ShiftWorkRecord("", 0)) else shiftWorks
        )
        binding.composeList.setContent { ShiftWorkDialogList(state) }

        binding.description.text = getString(
            if (isFirstSetup) R.string.shift_work_starting_date
            else R.string.shift_work_starting_date_edit
        ).format(formatDate(getDateFromJdnOfCalendar(mainCalendar, jdn)))

        binding.resetLink.setOnClickListener {
            jdn = selectedJdn
            binding.description.text = getString(R.string.shift_work_starting_date)
                .format(formatDate(getDateFromJdnOfCalendar(mainCalendar, jdn)))
            state.value = listOf(ShiftWorkRecord("", 0))
        }
        binding.recurs.isChecked = shiftWorkRecurs

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setTitle(null)
            .setPositiveButton(R.string.accept) { _, _ ->
                val result = state.value.filter { it.length != 0 }.joinToString(",") {
                    "${it.type.replace("=", "").replace(",", "")}=${it.length}"
                }

                activity.appPrefs.edit {
                    putLong(PREF_SHIFT_WORK_STARTING_JDN, if (result.isEmpty()) -1 else jdn)
                    putString(PREF_SHIFT_WORK_SETTING, result)
                    putBoolean(PREF_SHIFT_WORK_RECURS, binding.recurs.isChecked)
                }

                updateStoredPreference(activity)
                findNavController().navigate(CalendarFragmentDirections.navigateToSelf())
            }
            .setCancelable(true)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onResume() {
        super.onResume()

        // https://stackoverflow.com/a/46248107
        dialog?.window?.run {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = ShiftWorkDialog().apply {
            arguments = bundleOf(BUNDLE_KEY to jdn)
        }
    }
}
