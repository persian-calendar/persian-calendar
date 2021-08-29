package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShiftWorkItemBinding
import com.byagowi.persiancalendar.databinding.ShiftWorkSettingsBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.shiftWorkRecurs
import com.byagowi.persiancalendar.utils.shiftWorkStartingJdn
import com.byagowi.persiancalendar.utils.shiftWorkTitles
import com.byagowi.persiancalendar.utils.shiftWorks
import com.byagowi.persiancalendar.utils.spacedComma
import com.byagowi.persiancalendar.utils.updateStoredPreference

fun showShiftWorkDialog(context: Context, selectedJdn: Jdn, onSuccess: () -> Unit) {
    var isFirstSetup = false
    var jdn = shiftWorkStartingJdn ?: run {
        isFirstSetup = true
        selectedJdn
    }

    val binding = ShiftWorkSettingsBinding.inflate(context.layoutInflater, null, false)
    binding.recyclerView.layoutManager = LinearLayoutManager(context)
    val shiftWorkItemAdapter = ShiftWorkItemsAdapter(
        if (shiftWorks.isEmpty()) listOf(ShiftWorkRecord("d", 0)) else shiftWorks,
        binding
    )
    binding.recyclerView.adapter = shiftWorkItemAdapter

    binding.description.text = context.getString(
        if (isFirstSetup) R.string.shift_work_starting_date
        else R.string.shift_work_starting_date_edit,
        formatDate(jdn.toCalendar(mainCalendar))
    )

    binding.resetLink.setOnClickListener {
        jdn = selectedJdn
        binding.description.text = context.getString(
            R.string.shift_work_starting_date, formatDate(jdn.toCalendar(mainCalendar))
        )
        shiftWorkItemAdapter.reset()
    }
    binding.recurs.isChecked = shiftWorkRecurs
    binding.root.onCheckIsTextEditor()

    AlertDialog.Builder(context)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val result = shiftWorkItemAdapter.rows.filter { it.length != 0 }.joinToString(",") {
                "${it.type.replace("=", "").replace(",", "")}=${it.length}"
            }

            context.appPrefs.edit {
                putJdn(PREF_SHIFT_WORK_STARTING_JDN, if (result.isEmpty()) null else jdn)
                putString(PREF_SHIFT_WORK_SETTING, result)
                putBoolean(PREF_SHIFT_WORK_RECURS, binding.recurs.isChecked)
            }

            updateStoredPreference(context)
            onSuccess()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

private class ShiftWorkItemsAdapter(
    var rows: List<ShiftWorkRecord>, private val binding: ShiftWorkSettingsBinding
) : RecyclerView.Adapter<ShiftWorkItemsAdapter.ViewHolder>() {

    init {
        updateShiftWorkResult()
    }

    private fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type

    private fun updateShiftWorkResult() {
        rows.filter { it.length != 0 }.joinToString(spacedComma) {
            binding.root.context.getString(
                R.string.shift_work_record_title,
                formatNumber(it.length), shiftWorkKeyToString(it.type)
            )
        }.also {
            binding.result.text = it
            binding.result.isVisible = it.isNotEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ShiftWorkItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = rows.size + 1

    fun reset() {
        val previousSize = rows.size
        rows = listOf(ShiftWorkRecord("d", 0))
        notifyItemRangeChanged(0, 2)
        if (previousSize > 1) notifyItemRangeRemoved(2, previousSize + 1)
        updateShiftWorkResult()
    }

    inner class ViewHolder(private val binding: ShiftWorkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val context = binding.root.context

            binding.lengthSpinner.adapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item, (0..14).map {
                    if (it == 0) context.getString(R.string.shift_work_days_head)
                    else formatNumber(it)
                }
            )

            binding.typeAutoCompleteTextView.also { editText ->
                val adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_dropdown_item,
                    shiftWorkTitles.values.toList()
                )
                editText.setAdapter(adapter)
                editText.setOnClickListener {
                    if (editText.text.toString().isNotEmpty()) adapter.filter.filter(null)
                    editText.showDropDown()
                }
                editText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View, position: Int, id: Long
                    ) {
                        rows = rows.mapIndexed { i, x ->
                            if (i == bindingAdapterPosition)
                                ShiftWorkRecord(editText.text.toString(), x.length)
                            else x
                        }
                        updateShiftWorkResult()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) = Unit
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) = Unit

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                        rows = rows.mapIndexed { i, x ->
                            if (i == bindingAdapterPosition)
                                ShiftWorkRecord(editText.text.toString(), x.length)
                            else x
                        }
                        updateShiftWorkResult()
                    }
                })
                // Don't allow inserting '=' or ',' as they have special meaning
                editText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                    if (Regex("[=,]") in (source ?: "")) "" else null
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
                            if (i == bindingAdapterPosition) ShiftWorkRecord(x.type, position)
                            else x
                        }
                        updateShiftWorkResult()
                    }
                }

            binding.addButton.setOnClickListener {
                rows = rows + ShiftWorkRecord("r", 0)
                notifyItemInserted(bindingAdapterPosition)
                updateShiftWorkResult()
            }
        }

        fun remove() {
            rows = rows.filterIndexed { i, _ -> i != bindingAdapterPosition }
            notifyItemRemoved(bindingAdapterPosition)
            notifyItemRangeChanged(0, rows.size)
            updateShiftWorkResult()
        }

        fun bind(position: Int) = if (position < rows.size) {
            val shiftWorkRecord = rows[position]
            binding.rowNumber.text = "%s:".format(formatNumber(position + 1))
            binding.lengthSpinner.setSelection(shiftWorkRecord.length)
            binding.typeAutoCompleteTextView.setText(shiftWorkKeyToString(shiftWorkRecord.type))
            binding.detail.isVisible = true
            binding.addButton.isVisible = false
        } else {
            binding.detail.isVisible = false
            binding.addButton.isVisible = rows.size < 20
        }
    }
}
