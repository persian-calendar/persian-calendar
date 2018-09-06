package com.byagowi.persiancalendar.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
 
public class SettingsFragment extends Fragment {
    private Drawable mDrawable;
    private String mTitle;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_settings, null);
        ViewPager viewPager = view.findViewById(R.id.transactions_recharge);
        PagerAdapter mPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 3);
        viewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;
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
            switch (position){
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
                    mDrawable = getResources().getDrawable(R.drawable.ic_settings_general);
                    mTitle = getResources().getString(R.string.pref_header_language_calendar);
                    break;
                case 1:
                    mDrawable = getResources().getDrawable(R.drawable.ic_settings_widget);
                    mTitle = getResources().getString(R.string.pref_header_widget_location);
                    break;
                case 2:
                    mDrawable = getResources().getDrawable(R.drawable.ic_settings_location);
                    mTitle = getResources().getString(R.string.pref_header_location_athan);
                    break;
                default:
                    break;
            }
            SpannableStringBuilder sb = new SpannableStringBuilder("   " + mTitle); // space added before text for convenience
            try {
                mDrawable.setBounds(3, 3, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(mDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
                sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                // TODO: handle exception
            }
            return sb;
        }
    }
}