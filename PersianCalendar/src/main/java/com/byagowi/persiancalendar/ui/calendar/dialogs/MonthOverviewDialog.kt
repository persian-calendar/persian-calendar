package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewDialogBinding
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.Utils
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class MonthOverviewDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = mainActivityDependency.mainActivity
        var baseJdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L
        if (baseJdn == -1L) baseJdn = getTodayJdn()

        val records = ArrayList<MonthOverviewRecord>()

        val mainCalendar = getMainCalendar()
        val date = getDateFromJdnOfCalendar(mainCalendar, baseJdn)
        val monthLength = getMonthLength(mainCalendar, date.year, date.month).toLong()
        val deviceEvents = readMonthDeviceEvents(context, baseJdn)
        for (i in 0 until monthLength) {
            val jdn = baseJdn + i
            val events = Utils.getEvents(jdn, deviceEvents)
            val holidays = Utils.getEventsTitle(events, true, false, false, false)
            val nonHolidays = Utils.getEventsTitle(events, false, false, true, false)
            if (!(TextUtils.isEmpty(holidays) && TextUtils.isEmpty(nonHolidays)))
                records.add(MonthOverviewRecord(dayTitleSummary(
                        getDateFromJdnOfCalendar(mainCalendar, jdn)), holidays, nonHolidays))
        }
        if (records.size == 0)
            records.add(MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", ""))

        val binding = MonthOverviewDialogBinding.inflate(
                LayoutInflater.from(context), null, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = ItemAdapter(records)

        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setCanceledOnTouchOutside(true)
        return bottomSheetDialog
    }

    internal class MonthOverviewRecord(val title: String, val holidays: String, val nonHolidays: String)

    private inner class ItemAdapter internal constructor(private val mRows: List<MonthOverviewRecord>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = MonthOverviewItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = mRows.size

        internal inner class ViewHolder(var mBinding: MonthOverviewItemBinding) : RecyclerView.ViewHolder(mBinding.root) {

            fun bind(position: Int) {
                val record = mRows[position]
                mBinding.title.text = record.title
                mBinding.holidays.text = record.holidays
                mBinding.holidays.visibility = if (TextUtils.isEmpty(record.holidays)) View.GONE else View.VISIBLE
                mBinding.nonHolidays.text = record.nonHolidays
                mBinding.nonHolidays.visibility = if (TextUtils.isEmpty(record.nonHolidays)) View.GONE else View.VISIBLE
            }
        }
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = MonthOverviewDialog().apply {
            arguments = Bundle().apply {
                putLong(BUNDLE_KEY, jdn)
            }
        }
    }
}
