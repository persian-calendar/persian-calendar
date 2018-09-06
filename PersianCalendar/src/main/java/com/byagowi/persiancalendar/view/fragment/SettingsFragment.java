package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding;
import com.byagowi.persiancalendar.util.UIUtils;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        FragmentSettingsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(container.getContext()),
                R.layout.fragment_settings, container, false);

        binding.viewpager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), 3));
        binding.tabs.setupWithViewPager(binding.viewpager);

        return binding.getRoot();
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        int pageCount;

        ViewPagerAdapter(FragmentManager manager, int _pageCount) {
            super(manager);
            pageCount = _pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();
            switch (position) {
                case 0:
                    fragment = new FragmentLanguageCalendar();
                    break;
                case 1:
                    fragment = new FragmentWidgetNotification();
                    break;
                case 2:
                    fragment = new FragmentLocationAthan();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.pref_header_language_calendar);

                case 1:
                    return getResources().getString(R.string.pref_header_widget_location);

                default:
                case 2:
                    return getResources().getString(R.string.pref_header_location_athan);
            }
        }
    }
}