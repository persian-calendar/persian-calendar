package com.byagowi.persiancalendar.ui.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar;
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan;
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import dagger.android.support.DaggerFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class PreferencesFragment extends DaggerFragment {
    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainActivityDependency.getMainActivity().setTitleAndSubtitle(getString(R.string.settings), "");

        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(
                LayoutInflater.from(container.getContext()), container, false);

        binding.viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), 3));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        return binding.getRoot();
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        int pageCount;

        ViewPagerAdapter(FragmentManager manager, int pageCount) {
            super(manager);
            this.pageCount = pageCount;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();
            switch (position) {
                case 0:
                    fragment = new FragmentInterfaceCalendar();
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
                    return getResources().getString(R.string.pref_header_interface_calendar);

                case 1:
                    return getResources().getString(R.string.pref_header_widget_location);

                default:
                case 2:
                    return getResources().getString(R.string.pref_header_location_athan);
            }
        }
    }
}
