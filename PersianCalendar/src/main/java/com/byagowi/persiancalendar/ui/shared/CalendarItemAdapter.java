package com.byagowi.persiancalendar.ui.shared;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.databinding.CalendarItemBinding;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.TypefaceUtils;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemAdapter.ViewHolder> {
    private final Typeface mCalendarFont;
    private List<CalendarType> mCalendars = new ArrayList<>();
    private boolean mExpanded = false;
    private long mJdn;

    CalendarItemAdapter(Context context) {
        mCalendarFont = TypefaceUtils.getCalendarFragmentFont(context);
    }

    void setDate(List<CalendarType> calendars, long jdn) {
        mCalendars = calendars;
        mJdn = jdn;
        for (int i = 0; i < mCalendars.size(); ++i) notifyItemChanged(i);
//        notifyDataSetChanged();
    }

    boolean isExpanded() {
        return mExpanded;
    }

    void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
        for (int i = 0; i < mCalendars.size(); ++i) notifyItemChanged(i);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalendarItemBinding binding = CalendarItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mCalendars.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CalendarItemBinding binding;

        ViewHolder(CalendarItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            boolean applyLineMultiplier = !TypefaceUtils.isCustomFontEnabled();

            binding.monthYear.setTypeface(mCalendarFont);
            binding.day.setTypeface(mCalendarFont);
            if (applyLineMultiplier) binding.monthYear.setLineSpacing(0, .6f);

            binding.container.setOnClickListener(this);
            binding.linear.setOnClickListener(this);
        }

        public void bind(int position) {
            AbstractDate date = Utils.getDateFromJdnOfCalendar(mCalendars.get(position), mJdn);

            binding.linear.setText(Utils.toLinearDate(date));
            binding.linear.setContentDescription(Utils.toLinearDate(date));
            String firstCalendarString = Utils.formatDate(date);
            binding.container.setContentDescription(firstCalendarString);
            binding.day.setContentDescription("");
            binding.day.setText(Utils.formatNumber(date.getDayOfMonth()));
            binding.monthYear.setContentDescription("");
            binding.monthYear.setText(String.format("%s\n%s",
                    Utils.getMonthName(date),
                    Utils.formatNumber(date.getYear())));
        }

        @Override
        public void onClick(View view) {
            Utils.copyToClipboard(view, "converted date", view.getContentDescription());
        }
    }
}
