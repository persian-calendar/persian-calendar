package com.byagowi.persiancalendar.ui.calendar.shiftwork

import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShiftWorkItemBinding
import com.byagowi.persiancalendar.databinding.ShiftWorkSettingsBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.shiftWorkRecurs
import com.byagowi.persiancalendar.global.shiftWorkStartingJdn
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.shiftWorks
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showShiftWorkDialog(activity: FragmentActivity, selectedJdn: Jdn) {
    var isFirstSetup = false
    var jdn = shiftWorkStartingJdn ?: run {
        isFirstSetup = true
        selectedJdn
    }

    val binding = ShiftWorkSettingsBinding.inflate(activity.layoutInflater, null, false)
    binding.recyclerView.layoutManager = LinearLayoutManager(activity)
    val shiftWorkItemAdapter = ShiftWorkItemsAdapter(
        shiftWorks.ifEmpty { listOf(ShiftWorkRecord("d", 0)) },
        binding
    )
    binding.recyclerView.adapter = shiftWorkItemAdapter

    binding.description.text = activity.getString(
        if (isFirstSetup) R.string.shift_work_starting_date
        else R.string.shift_work_starting_date_edit,
        formatDate(jdn.toCalendar(mainCalendar))
    )

    binding.resetLink.setOnClickListener {
        jdn = selectedJdn
        binding.description.text = activity.getString(
            R.string.shift_work_starting_date, formatDate(jdn.toCalendar(mainCalendar))
        )
        shiftWorkItemAdapter.reset()
    }
    binding.recurs.isChecked = shiftWorkRecurs
    binding.root.onCheckIsTextEditor()

    MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            saveShiftWorkState(activity, shiftWorkItemAdapter.rows, jdn, binding.recurs.isChecked)
        }
        .setNegativeButton(R.string.cancel, null)
        .create()
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
            binding.root.context.resources.getQuantityString(
                R.plurals.shift_work_record_title, it.length,
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
                context, android.R.layout.simple_spinner_dropdown_item, (0..14).map(::formatNumber)
            )

            binding.editText.also { editText ->
                editText.setAdapter(object : ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_dropdown_item
                ) {
                    private val titles = shiftWorkTitles.values.toList()
                    override fun getItem(position: Int) = titles[position]
                    override fun getCount(): Int = titles.size
                })
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
                editText.doOnTextChanged { text, _, _, _ ->
                    rows = rows.mapIndexed { i, x ->
                        if (i == bindingAdapterPosition) ShiftWorkRecord(text.toString(), x.length)
                        else x
                    }
                    updateShiftWorkResult()
                }
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
                notifyItemChanged(rows.size) // ensure the add button will be removed after a certain size
                updateShiftWorkResult()
            }
        }

        fun remove() {
            rows = rows.filterIndexed { i, _ -> i != bindingAdapterPosition }
            notifyItemRemoved(bindingAdapterPosition)
            notifyItemRangeChanged(0, rows.size)
            notifyItemChanged(rows.size) // ensure the add button will be re-added
            updateShiftWorkResult()
        }

        fun bind(position: Int) = if (position < rows.size) {
            val shiftWorkRecord = rows[position]
            binding.editTextParent.prefixText = "\n${formatNumber(position + 1)}$spacedColon"
            binding.editTextParent.prefixTextView.textSize = 12f
            binding.lengthSpinner.setSelection(shiftWorkRecord.length)
            binding.editText.setText(shiftWorkKeyToString(shiftWorkRecord.type))
            binding.detail.isVisible = true
            binding.addButton.isVisible = false
        } else {
            binding.detail.isVisible = false
            binding.addButton.isVisible = rows.size < 20
        }
    }
}
