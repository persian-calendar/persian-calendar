package com.byagowi.persiancalendar.ui.calendar.shiftwork

import android.content.DialogInterface
import android.os.Build
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun showShiftWorkDialog(activity: FragmentActivity, selectedJdn: Jdn) {
    val viewModel = ShiftWorkViewModel()
    // from already initialized global variable till a better solution
    fillViewModelFromGlobalVariables(viewModel, selectedJdn)

    val binding = ShiftWorkSettingsBinding.inflate(activity.layoutInflater, null, false)
    binding.recyclerView.layoutManager = LinearLayoutManager(activity)
    val shiftWorkItemAdapter = ShiftWorkItemsAdapter(viewModel.shiftWorks.value, binding)
    binding.recyclerView.adapter = shiftWorkItemAdapter

    // TODO: Follow viewModel.isFirstSetup instead
    binding.description.text = activity.getString(
        if (viewModel.isFirstSetup.value) R.string.shift_work_starting_date
        else R.string.shift_work_starting_date_edit,
        formatDate(viewModel.startingDate.value.toCalendar(mainCalendar))
    )

    binding.resetLink.setOnClickListener {
        viewModel.changeStartingDate(selectedJdn)
        // TODO: Merge with above and follow viewModel.isFirstSetup instead
        binding.description.text = activity.getString(
            R.string.shift_work_starting_date,
            formatDate(viewModel.startingDate.value.toCalendar(mainCalendar))
        )
        // TODO: Move some parts of it to view model itself
        shiftWorkItemAdapter.reset()
    }
    binding.recurs.isChecked = viewModel.recurs.value
    binding.recurs.setOnCheckedChangeListener { _, isChecked -> viewModel.changeRecurs(isChecked) }
    binding.root.onCheckIsTextEditor()

    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            viewModel.changeShiftWorks(shiftWorkItemAdapter.rows)
            saveShiftWorkState(activity, viewModel)
        }
        .setNeutralButton(R.string.add, null)
        .setNegativeButton(R.string.cancel, null)
        .show()
    val addButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL).debugAssertNotNull
    shiftWorkItemAdapter.addButton = addButton
    addButton?.setOnClickListener { shiftWorkItemAdapter.add() }
}

private class ShiftWorkItemsAdapter(
    var rows: List<ShiftWorkRecord>,
    private val binding: ShiftWorkSettingsBinding,
    var addButton: Button? = null
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
        addButton?.isEnabled = rows.size < 40
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ShiftWorkItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = rows.size

    fun reset() {
        val previousSize = rows.size
        rows = emptyList()
        notifyItemRangeChanged(0, 2)
        if (previousSize > 1) notifyItemRangeRemoved(2, previousSize + 1)
        updateShiftWorkResult()
    }

    fun add() {
        rows = rows + ShiftWorkRecord("r", 1)
        notifyItemInserted(rows.size - 1)
        notifyItemChanged(rows.size) // ensure the add button will be removed after a certain size
        updateShiftWorkResult()
        // Scroll to button on addition
        binding.recyclerView.scrollToPosition(rows.size - 1)
    }

    inner class ViewHolder(private val binding: ShiftWorkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val context = binding.root.context

            binding.lengthSpinner.adapter = ArrayAdapter(
                context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                (1..14).map(::formatNumber)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.lengthSpinner.setPopupBackgroundResource(R.drawable.popup_background)
            }

            binding.editText.also { editText ->
                editText.setAdapter(object : ArrayAdapter<String>(
                    context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item
                ) {
                    private val titles =
                        shiftWorkTitles.values.toList() + language.additionalShiftWorkTitles

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
                            if (i == bindingAdapterPosition) ShiftWorkRecord(x.type, position + 1)
                            else x
                        }
                        updateShiftWorkResult()
                    }
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
            binding.lengthSpinner.setSelection(shiftWorkRecord.length - 1)
            binding.editText.setText(shiftWorkKeyToString(shiftWorkRecord.type))
            binding.detail.isVisible = true
        } else {
            binding.detail.isVisible = false
        }
    }
}
