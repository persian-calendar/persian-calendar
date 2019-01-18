package com.byagowi.persiancalendar.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.TimeItemBinding;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.util.UIUtils;
import com.google.android.flexbox.FlexboxLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

public class TimeItemAdapter extends RecyclerView.Adapter<TimeItemAdapter.ViewHolder> {
    @StringRes
    static final private int[] timeNames = new int[]{
            R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
            R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
    };
    private PrayTimes mPrayTimes;
    private boolean mExpanded = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TimeItemBinding binding = TimeItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return timeNames.length;
    }

    public void setTimes(PrayTimes prayTimes) {
        mPrayTimes = prayTimes;
        for (int i = 0; i < timeNames.length; ++i) notifyItemChanged(i);
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        for (int i = 0; i < timeNames.length; ++i) notifyItemChanged(i);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TimeItemBinding binding;
        private FlexboxLayoutManager.LayoutParams emptyLayout =
                new FlexboxLayoutManager.LayoutParams(0, 0);
        private FlexboxLayoutManager.LayoutParams wrapContent =
                new FlexboxLayoutManager.LayoutParams(
                        FlexboxLayoutManager.LayoutParams.WRAP_CONTENT,
                        FlexboxLayoutManager.LayoutParams.WRAP_CONTENT);

        ViewHolder(TimeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            int timeName = timeNames[position];
            binding.getRoot().setLayoutParams(!mExpanded && !(timeName == R.string.fajr ||
                    timeName == R.string.dhuhr || timeName == R.string.maghrib)
                    ? emptyLayout : wrapContent);

            binding.name.setText(timeName);

            if (mPrayTimes == null) {
                binding.time.setText("");
                return;
            }

            Clock clock;
            switch (timeName) {
                case R.string.imsak:
                    clock = mPrayTimes.getImsakClock();
                    break;
                case R.string.fajr:
                    clock = mPrayTimes.getFajrClock();
                    break;
                case R.string.sunrise:
                    clock = mPrayTimes.getSunriseClock();
                    break;
                case R.string.dhuhr:
                    clock = mPrayTimes.getDhuhrClock();
                    break;
                case R.string.asr:
                    clock = mPrayTimes.getAsrClock();
                    break;
                case R.string.sunset:
                    clock = mPrayTimes.getSunsetClock();
                    break;
                case R.string.maghrib:
                    clock = mPrayTimes.getMaghribClock();
                    break;
                case R.string.isha:
                    clock = mPrayTimes.getIshaClock();
                    break;
                default:
                case R.string.midnight:
                    clock = mPrayTimes.getMidnightClock();
                    break;
            }
            binding.time.setText(UIUtils.getFormattedClock(clock, false));
        }
    }
}
