package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShiftWorkItemBinding
import com.byagowi.persiancalendar.databinding.ShiftWorkSettingsBinding
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.di.CalendarFragmentDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.entities.StringWithValueItem
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.DaggerAppCompatDialogFragment
import java.util.*
import javax.inject.Inject


class ShiftWorkDialog : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    @Inject
    lateinit var calendarFragmentDependency: CalendarFragmentDependency

    private var jdn: Long = -1L
    private var selectedJdn: Long = -1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainActivity = mainActivityDependency.mainActivity

        Utils.applyAppLanguage(mainActivity)
        Utils.updateStoredPreference(mainActivity)

        selectedJdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L
        if (selectedJdn == -1L) selectedJdn = Utils.getTodayJdn()

        jdn = Utils.getShiftWorkStartingJdn()
        var isFirstSetup = false
        if (jdn == -1L) {
            isFirstSetup = true
            jdn = selectedJdn
        }

        val binding = ShiftWorkSettingsBinding.inflate(
                LayoutInflater.from(mainActivity), null, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)

        var shiftWorks: List<ShiftWorkRecord> = Utils.getShiftWorks()
        if (shiftWorks.isEmpty())
            shiftWorks = listOf(ShiftWorkRecord("d", 0))
        val shiftWorkItemAdapter = ItemsAdapter(shiftWorks, binding)
        binding.recyclerView.adapter = shiftWorkItemAdapter

        binding.description.text = String.format(getString(
                if (isFirstSetup) R.string.shift_work_starting_date else R.string.shift_work_starting_date_edit),
                Utils.formatDate(
                        Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn)))

        binding.resetLink.setOnClickListener {
            jdn = selectedJdn
            binding.description.text = String.format(getString(R.string.shift_work_starting_date),
                    Utils.formatDate(
                            Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn)))
            shiftWorkItemAdapter.reset()
        }
        binding.recurs.isChecked = Utils.getShiftWorkRecurs()

        return AlertDialog.Builder(mainActivity)
                .setView(binding.root)
                .setTitle(null)
                .setPositiveButton(R.string.accept) { _, _ ->
                    val result = StringBuilder()
                    var first = true
                    for (record in shiftWorkItemAdapter.rows) {
                        if (record.length == 0) continue

                        if (first)
                            first = false
                        else
                            result.append(",")
                        result.append(record.type.replace(Regex("[=,]"), ""))
                        result.append("=")
                        result.append(record.length)
                    }

                    appDependency.sharedPreferences.edit {
                        putLong(PREF_SHIFT_WORK_STARTING_JDN, if (result.isEmpty()) -1 else jdn)
                        putString(PREF_SHIFT_WORK_SETTING, result.toString())
                        putBoolean(PREF_SHIFT_WORK_RECURS, binding.recurs.isChecked)
                    }

                    calendarFragmentDependency.calendarFragment.afterShiftWorkChange()
                    mainActivity.restartActivity()
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


    private inner class ItemsAdapter internal constructor(initialItems: List<ShiftWorkRecord>, private val mBinding: ShiftWorkSettingsBinding) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        internal var mShiftWorkKeys: List<String>
        private val mRows = ArrayList<ShiftWorkRecord>()

        internal val rows: List<ShiftWorkRecord>
            get() = mRows

        init {
            mRows.addAll(initialItems)
            mShiftWorkKeys = listOf(*resources.getStringArray(R.array.shift_work_keys))
            updateShiftWorkResult()
        }

        fun shiftWorkKeyToString(type: String): String = Utils.getShiftWorkTitles()[type] ?: type

        private fun updateShiftWorkResult() {
            val result = StringBuilder()
            var first = true
            for (record in mRows) {
                if (record.length == 0) continue

                if (first)
                    first = false
                else
                    result.append(Utils.getSpacedComma())
                result.append(String.format(getString(R.string.shift_work_record_title),
                        Utils.formatNumber(record.length), shiftWorkKeyToString(record.type)))
            }

            mBinding.result.text = result.toString()
            mBinding.result.visibility = if (result.isEmpty()) View.GONE else View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ShiftWorkItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount(): Int = mRows.size + 1

        internal fun reset() {
            mRows.clear()
            mRows.add(ShiftWorkRecord("d", 0))
            notifyDataSetChanged()
            updateShiftWorkResult()
        }

        internal inner class ViewHolder(private val mBinding: ShiftWorkItemBinding) : RecyclerView.ViewHolder(mBinding.root) {
            private var mPosition: Int = 0

            init {
                val context = mBinding.root.context

                val days = ArrayList<StringWithValueItem>()
                for (i in 0..7) {
                    days.add(StringWithValueItem(i, if (i == 0)
                        getString(R.string.shift_work_days_head)
                    else
                        Utils.formatNumber(i)))
                }
                mBinding.lengthSpinner.adapter = ArrayAdapter(context,
                        android.R.layout.simple_spinner_dropdown_item, days)

                mBinding.typeAutoCompleteTextView.run {
                    val adapter = ArrayAdapter(context,
                            android.R.layout.simple_spinner_dropdown_item,
                            resources.getStringArray(R.array.shift_work))
                    setAdapter(adapter)
                    setOnClickListener {
                        if (text.toString().isNotEmpty()) adapter.filter.filter(null)
                        showDropDown()
                    }
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            mRows[mPosition] = ShiftWorkRecord(text.toString(), mRows[mPosition].length)
                            updateShiftWorkResult()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {}

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            mRows[mPosition] = ShiftWorkRecord(text.toString(), mRows[mPosition].length)
                            updateShiftWorkResult()
                        }
                    })
                    filters = arrayOf(object : InputFilter {
                        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                            return if (source?.contains("[=,]".toRegex()) == true) "" else null
                        }
                    })
                }

                mBinding.remove.setOnClickListener { remove() }

                mBinding.lengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        mRows[mPosition] = ShiftWorkRecord(
                                mRows[mPosition].type, position)
                        updateShiftWorkResult()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                mBinding.addButton.setOnClickListener {
                    mRows.add(ShiftWorkRecord("r", 0))
                    notifyDataSetChanged()
                    updateShiftWorkResult()
                }
            }

            fun remove() {
                mRows.removeAt(mPosition)
                notifyDataSetChanged()
                updateShiftWorkResult()
            }

            fun bind(position: Int) {
                if (position < mRows.size) {
                    val shiftWorkRecord = mRows[position]
                    mPosition = position
                    mBinding.rowNumber.text = String.format("%s:", Utils.formatNumber(position + 1))
                    mBinding.lengthSpinner.setSelection(shiftWorkRecord.length)
                    mBinding.typeAutoCompleteTextView.setText(shiftWorkKeyToString(shiftWorkRecord.type))
                    mBinding.detail.visibility = View.VISIBLE
                    mBinding.addButton.visibility = View.GONE
                } else {
                    mBinding.detail.visibility = View.GONE
                    mBinding.addButton.visibility = if (mRows.size < 20) View.VISIBLE else View.GONE
                }
            }
        }
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = ShiftWorkDialog().apply {
            arguments = Bundle().apply {
                putLong(BUNDLE_KEY, jdn)
            }
        }
    }
}
