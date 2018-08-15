package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.sunrisesunset.SunView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class CardTabsAdapter extends FragmentStatePagerAdapter {
    // don't remove public ever
    public static class TabFragment extends Fragment {
        private View view;

        static TabFragment newInstance(View view) {
            TabFragment tabFragment = new TabFragment();
            tabFragment.view = view;
            return tabFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return view;
        }
    }

    private Context context;
    private List<View> tabs;
    private List<String> titles;

    public CardTabsAdapter(FragmentManager fm, boolean isRTL, Context context,
                           List<View> tabs, List<String> titles) {
        super(fm);
        this.tabs = tabs;
        this.titles = titles;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(tabs.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    private int mCurrentPosition = -1;

    // https://stackoverflow.com/a/47774679
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        if (position != mCurrentPosition && container instanceof CardsViewPager) {
            Fragment fragment = (Fragment) object;
            CardsViewPager pager = (CardsViewPager) container;

            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                View tab = fragment.getView();
                pager.measureCurrentView(tab);

                View sunView = tab.findViewById(R.id.svPlot);
                if (sunView != null && sunView instanceof SunView) {
                    SunView sun = (SunView) sunView;
                    sun.startAnimate();
                }
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.LAST_CHOSEN_TAB_KEY, position);
        editor.apply();
    }
}