package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerAppCompatDialogFragment;

public class MonthOverviewDialog extends DaggerAppCompatDialogFragment {
    private static String BUNDLE_KEY = "jdn";
    @Inject
    AppDependency appDependency;
    @Inject
    MainActivityDependency mainActivityDependency;
    @Inject
    CalendarFragmentDependency calendarFragmentDependency;
    private long baseJdn = -1;

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
        MainActivity mainActivity = mainActivityDependency.getMainActivity();

        baseJdn = args == null ? -1 : args.getLong(BUNDLE_KEY, -1);
        if (baseJdn == -1) baseJdn = Utils.getTodayJdn();

        List<MonthOverviewRecord> records = new ArrayList<>();

        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate date = Utils.getDateFromJdnOfCalendar(mainCalendar, baseJdn);
        long monthLength = Utils.getMonthLength(mainCalendar, date.getYear(), date.getMonth());
        SparseArray<List<DeviceCalendarEvent>> deviceEvents = Utils.readMonthDeviceEvents(mainActivity, baseJdn);
        for (long i = 0; i < monthLength; ++i) {
            long jdn = baseJdn + i;
            List<AbstractEvent> events = Utils.getEvents(jdn, deviceEvents);
            String holidays = Utils.getEventsTitle(events, true, false, false, false);
            String nonHolidays = Utils.getEventsTitle(events, false, false, true, false);
            if (!(TextUtils.isEmpty(holidays) && TextUtils.isEmpty(nonHolidays)))
                records.add(new MonthOverviewRecord(Utils.dayTitleSummary(
                        Utils.getDateFromJdnOfCalendar(mainCalendar, jdn)), holidays, nonHolidays));
        }

        RecyclerView recyclerView = new RecyclerView(mainActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        recyclerView.setAdapter(new ItemAdapter(records));

        return new AlertDialog.Builder(mainActivity)
                .setView(recyclerView)
                .setTitle(null)
                .setCancelable(true)
                .setNegativeButton(R.string.closeDrawer, null)
                .create();
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
