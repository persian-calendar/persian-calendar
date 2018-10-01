package com.byagowi.persiancalendar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.SingleTimeBinding;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.util.UIUtils;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TimesHolderAdapter extends RecyclerView.Adapter<TimesHolderAdapter.ViewHolder> {
    @IdRes
    static final private int[] timeNames = new int[]{
            R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
            R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
    };
    private PrayTimes mPrayTimes;
    private boolean mExpanded = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleTimeBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.single_time, parent, false);

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
        for (int i = 0; i < 9; ++i) notifyItemChanged(i);
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        for (int i = 0; i < 9; ++i) notifyItemChanged(i);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private SingleTimeBinding binding;

        ViewHolder(SingleTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            int timeName = timeNames[position];
            binding.container.setVisibility(!mExpanded && !(timeName == R.string.fajr ||
                    timeName == R.string.dhuhr || timeName == R.string.maghrib)
                    ? View.GONE : View.VISIBLE);

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
            binding.time.setText(UIUtils.getFormattedClock(clock));
        }
    }
}
