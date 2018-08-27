package com.byagowi.persiancalendar.adapter;

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

    private List<View> tabs;
    private List<String> titles;

    public CardTabsAdapter(FragmentManager fm, List<View> tabs, List<String> titles) {
        super(fm);
        this.tabs = tabs;
        this.titles = titles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(tabs.get(position));
    }

    @Override
    public int getCount() {
        return tabs.size();
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
                View tab = fragment.getView();
                pager.measureCurrentView(tab);

                if (tabs.size() > 2) {
                    View sunView = tabs.get(Constants.OWGHAT_TAB).findViewById(R.id.svPlot);
                    if (sunView != null && sunView instanceof SunView) {
                        SunView sun = (SunView) sunView;
                        if (position == Constants.OWGHAT_TAB) {
                            sun.startAnimate(false);
                        } else {
                            sun.clear();
                        }
                    }
                }

                mCurrentPosition = position;
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                container.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.LAST_CHOSEN_TAB_KEY, position);
        editor.apply();
    }
}