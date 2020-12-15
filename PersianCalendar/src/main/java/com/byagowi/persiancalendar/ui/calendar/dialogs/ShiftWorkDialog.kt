package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShiftWorkItemBinding
import com.byagowi.persiancalendar.databinding.ShiftWorkSettingsBinding
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.*

class ShiftWorkDialog : AppCompatDialogFragment() {

    private var jdn: Long = -1L
    private var selectedJdn: Long = -1L

    var onSuccess = fun() {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainActivity = activity as MainActivity

        applyAppLanguage(mainActivity)
        updateStoredPreference(mainActivity)

        selectedJdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L
        if (selectedJdn == -1L) selectedJdn = getTodayJdn()

        jdn = shiftWorkStartingJdn
        var isFirstSetup = false
        if (jdn == -1L) {
            isFirstSetup = true
            jdn = selectedJdn
        }

        val binding = ShiftWorkSettingsBinding.inflate(mainActivity.layoutInflater, null, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        val shiftWorkItemAdapter = ItemsAdapter(
                if (shiftWorks.isEmpty()) listOf(ShiftWorkRecord("d", 0)) else shiftWorks,
                binding
        )
        binding.recyclerView.adapter = shiftWorkItemAdapter

        binding.description.text = getString(
                if (isFirstSetup) R.string.shift_work_starting_date
                else R.string.shift_work_starting_date_edit
        ).format(formatDate(getDateFromJdnOfCalendar(mainCalendar, jdn)))

        binding.resetLink.setOnClickListener {
            jdn = selectedJdn
            binding.description.text = getString(R.string.shift_work_starting_date)
                    .format(formatDate(getDateFromJdnOfCalendar(mainCalendar, jdn)))
            shiftWorkItemAdapter.reset()
        }
        binding.recurs.isChecked = shiftWorkRecurs

        return AlertDialog.Builder(mainActivity)
                .setView(binding.root)
                .setTitle(null)
                .setPositiveButton(R.string.accept) { _, _ ->
                    val result = shiftWorkItemAdapter.rows.filter { it.length != 0 }.joinToString(",") {
                        "${it.type.replace("=", "").replace(",", "")}=${it.length}"
                    }

                    mainActivity.appPrefs.edit {
                        putLong(PREF_SHIFT_WORK_STARTING_JDN, if (result.isEmpty()) -1 else jdn)
                        putString(PREF_SHIFT_WORK_SETTING, result)
                        putBoolean(PREF_SHIFT_WORK_RECURS, binding.recurs.isChecked)
                    }

                    onSuccess()
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

    private inner class ItemsAdapter(
            var rows: List<ShiftWorkRecord> = emptyList(),
            private val binding: ShiftWorkSettingsBinding
    ) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

        init {
            updateShiftWorkResult()
        }

        fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type

        private fun updateShiftWorkResult() =
                rows.filter { it.length != 0 }.joinToString(spacedComma) {
                    getString(R.string.shift_work_record_title)
                            .format(formatNumber(it.length), shiftWorkKeyToString(it.type))
                }.also {
                    binding.result.text = it
                    binding.result.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
                ShiftWorkItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount(): Int = rows.size + 1

        fun reset() {
            rows = listOf(ShiftWorkRecord("d", 0))
            notifyDataSetChanged()
            updateShiftWorkResult()
        }

        inner class ViewHolder(private val binding: ShiftWorkItemBinding) :
                RecyclerView.ViewHolder(binding.root) {

            init {
                val context = binding.root.context

                binding.lengthSpinner.adapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        (0..7).map {
                            if (it == 0) getString(R.string.shift_work_days_head) else formatNumber(it)
                        }
                )

                binding.typeAutoCompleteTextView.run {
                    val adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_dropdown_item,
                            resources.getStringArray(R.array.shift_work)
                    )
                    setAdapter(adapter)
                    setOnClickListener {
                        if (text.toString().isNotEmpty()) adapter.filter.filter(null)
                        showDropDown()
                    }
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                                parent: AdapterView<*>, view: View, position: Int, id: Long
                        ) {
                            rows = rows.mapIndexed { i, x ->
                                if (i == adapterPosition) ShiftWorkRecord(
                                        text.toString(), rows[adapterPosition].length
                                )
                                else x
                            }
                            updateShiftWorkResult()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {}

                        override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                        ) = Unit

                        override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
                        ) {
                            rows = rows.mapIndexed { i, x ->
                                if (i == adapterPosition) ShiftWorkRecord(
                                        text.toString(),
                                        rows[adapterPosition].length
                                )
                                else x
                            }
                            updateShiftWorkResult()
                        }
                    })
                    filters = arrayOf(object : InputFilter {
                        override fun filter(
                                source: CharSequence?, start: Int, end: Int,
                                dest: Spanned?, dstart: Int, dend: Int
                        ) = if ("[=,]".toRegex() in (source ?: "")) "" else null
                    })
                }

                binding.remove.setOnClickListener { remove() }

                binding.lengthSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                            override fun onItemSelected(
                                    parent: AdapterView<*>, view: View, position: Int, id: Long
                            ) {
                                rows = rows.mapIndexed { i, x ->
                                    if (i == adapterPosition) ShiftWorkRecord(x.type, position)
                                    else x
                                }
                                updateShiftWorkResult()
                            }
                        }

                binding.addButton.setOnClickListener {
                    rows = rows + ShiftWorkRecord("r", 0)
                    notifyDataSetChanged()
                    updateShiftWorkResult()
                }
            }

            fun remove() {
                rows = rows.filterIndexed { i, _ -> i != adapterPosition }
                notifyDataSetChanged()
                updateShiftWorkResult()
            }

            fun bind(position: Int) = if (position < rows.size) {
                val shiftWorkRecord = rows[position]
                binding.rowNumber.text = "%s:".format(formatNumber(position + 1))
                binding.lengthSpinner.setSelection(shiftWorkRecord.length)
                binding.typeAutoCompleteTextView.setText(shiftWorkKeyToString(shiftWorkRecord.type))
                binding.detail.visibility = View.VISIBLE
                binding.addButton.visibility = View.GONE
            } else {
                binding.detail.visibility = View.GONE
                binding.addButton.visibility = if (rows.size < 20) View.VISIBLE else View.GONE
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
