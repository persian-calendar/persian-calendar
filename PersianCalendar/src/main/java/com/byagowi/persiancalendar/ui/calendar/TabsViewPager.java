package com.byagowi.persiancalendar.ui.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.ui.calendar.times.SunView;
import com.duolingo.open.rtlviewpager.RtlViewPager;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

// https://stackoverflow.com/a/47774679
public class TabsViewPager extends RtlViewPager {
    private View mCurrentView;

    public TabsViewPager(Context context) {
        super(context);
    }

    public TabsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentView != null) {
            int height = 0;
            mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = mCurrentView.getMeasuredHeight();
            if (h > height) height = h;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureCurrentView(View currentView) {
        mCurrentView = currentView;
        requestLayout();
    }

    public static class TabsAdapter extends FragmentStatePagerAdapter {
        final private List<View> mTabs;
        final private List<String> mTitles;
        final private AppDependency mAppDependency;
        private int mCurrentPosition = -1;

        TabsAdapter(FragmentManager fm, AppDependency appDependency, List<View> tabs, List<String> titles) {
            super(fm);
            mTabs = tabs;
            mTitles = titles;
            mAppDependency = appDependency;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return TabFragment.newInstance(mTabs.get(position));
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        // https://stackoverflow.com/a/47774679
        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);

            if (position != mCurrentPosition && container instanceof TabsViewPager) {
                Fragment fragment = (Fragment) object;
                TabsViewPager pager = (TabsViewPager) container;

                if (fragment.getView() != null) {
                    View tab = fragment.getView();
                    pager.measureCurrentView(tab);

                    if (mTabs.size() > 2) {
                        View sunView = mTabs.get(Constants.OWGHAT_TAB).findViewById(R.id.sunView);
                        if (sunView instanceof SunView) {
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

            SharedPreferences.Editor editor = mAppDependency.getSharedPreferences().edit();
            editor.putInt(Constants.LAST_CHOSEN_TAB_KEY, position);
            editor.apply();
        }

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
            public View onCreateView(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container, Bundle savedInstanceState) {
                return view;
            }
        }
    }
}
