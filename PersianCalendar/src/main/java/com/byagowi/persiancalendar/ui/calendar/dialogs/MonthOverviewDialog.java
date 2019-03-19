package com.byagowi.persiancalendar.ui.calendar.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.databinding.MonthOverviewDialogBinding;
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MonthOverviewDialog extends BottomSheetDialogFragment {
    private static String BUNDLE_KEY = "jdn";

    public static MonthOverviewDialog newInstance(long jdn) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY, jdn);

        MonthOverviewDialog fragment = new MonthOverviewDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Context context = getContext();

        long baseJdn = args == null ? -1 : args.getLong(BUNDLE_KEY, -1);
        if (baseJdn == -1) baseJdn = Utils.getTodayJdn();

        List<MonthOverviewRecord> records = new ArrayList<>();

        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate date = Utils.getDateFromJdnOfCalendar(mainCalendar, baseJdn);
        long monthLength = Utils.getMonthLength(mainCalendar, date.getYear(), date.getMonth());
        SparseArray<List<DeviceCalendarEvent>> deviceEvents = Utils.readMonthDeviceEvents(context, baseJdn);
        for (long i = 0; i < monthLength; ++i) {
            long jdn = baseJdn + i;
            List<AbstractEvent> events = Utils.getEvents(jdn, deviceEvents);
            String holidays = Utils.getEventsTitle(events, true, false, false, false);
            String nonHolidays = Utils.getEventsTitle(events, false, false, true, false);
            if (!(TextUtils.isEmpty(holidays) && TextUtils.isEmpty(nonHolidays)))
                records.add(new MonthOverviewRecord(Utils.dayTitleSummary(
                        Utils.getDateFromJdnOfCalendar(mainCalendar, jdn)), holidays, nonHolidays));
        }
        if (records.size() == 0)
            records.add(new MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", ""));

        MonthOverviewDialogBinding binding = MonthOverviewDialogBinding.inflate(
                LayoutInflater.from(context), null, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerView.setAdapter(new ItemAdapter(records));

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        return bottomSheetDialog;
    }

    static class MonthOverviewRecord {
        final String title;
        final String holidays;
        final String nonHolidays;

        MonthOverviewRecord(String title, String holidays, String nonHolidays) {
            this.title = title;
            this.holidays = holidays;
            this.nonHolidays = nonHolidays;
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
        private List<MonthOverviewRecord> mRows;

        ItemAdapter(List<MonthOverviewRecord> rows) {
            mRows = rows;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MonthOverviewItemBinding binding = MonthOverviewItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);

            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mRows.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MonthOverviewItemBinding mBinding;

            ViewHolder(@NonNull MonthOverviewItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void bind(int position) {
                MonthOverviewRecord record = mRows.get(position);
                mBinding.title.setText(record.title);
                mBinding.holidays.setText(record.holidays);
                mBinding.holidays.setVisibility(TextUtils.isEmpty(record.holidays) ? View.GONE : View.VISIBLE);
                mBinding.nonHolidays.setText(record.nonHolidays);
                mBinding.nonHolidays.setVisibility(TextUtils.isEmpty(record.nonHolidays) ? View.GONE : View.VISIBLE);
            }
        }
    }
}
