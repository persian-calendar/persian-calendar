/**
 * Copyright 2016 Duolingo
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duolingo.open.rtlviewpager;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * <code>RtlViewPager</code> is an API-compatible implemetation of <code>ViewPager</code> which
 * orders paged views according to the layout direction of the view.  In left to right mode, the
 * first view is at the left side of the carousel, and in right to left mode it is at the right
 * side.
 * <p>
 * It accomplishes this by wrapping the provided <code>PagerAdapter</code> and any provided
 * <code>OnPageChangeListener</code>s so that clients can be agnostic to layout direction and
 * modifications are kept internal to <code>RtlViewPager</code>.
 */
public class RtlViewPager extends ViewPager {
    private int mLayoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR;
    private HashMap<OnPageChangeListener, ReversingOnPageChangeListener> mPageChangeListeners = new HashMap<>();

    public RtlViewPager(Context context) {
        super(context);
    }

    public RtlViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        int viewCompatLayoutDirection = layoutDirection == View.LAYOUT_DIRECTION_RTL
                ? ViewCompat.LAYOUT_DIRECTION_RTL
                : ViewCompat.LAYOUT_DIRECTION_LTR;
        if (viewCompatLayoutDirection != mLayoutDirection) {
            PagerAdapter adapter = super.getAdapter();
            int position = 0;
            if (adapter != null) {
                position = getCurrentItem();
            }
            mLayoutDirection = viewCompatLayoutDirection;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                setCurrentItem(position);
            }
        }
    }

    @Override
    public PagerAdapter getAdapter() {
        ReversingAdapter adapter = (ReversingAdapter) super.getAdapter();
        return adapter == null ? null : adapter.getDelegate();
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter != null) {
            adapter = new ReversingAdapter(adapter);
        }
        super.setAdapter(adapter);
        setCurrentItem(0);
    }

    private boolean isRtl() {
        return mLayoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public int getCurrentItem() {
        int item = super.getCurrentItem();
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            item = adapter.getCount() - item - 1;
        }
        return item;
    }

    @Override
    public void setCurrentItem(int position) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            position = adapter.getCount() - position - 1;
        }
        super.setCurrentItem(position);
    }

    @Override
    public void setCurrentItem(int position, boolean smoothScroll) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            position = adapter.getCount() - position - 1;
        }
        super.setCurrentItem(position, smoothScroll);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mLayoutDirection);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        mLayoutDirection = ss.mLayoutDirection;
        super.onRestoreInstanceState(ss.mViewPagerSavedState);
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        ReversingOnPageChangeListener reversingListener = new ReversingOnPageChangeListener(listener);
        mPageChangeListeners.put(listener, reversingListener);
        super.addOnPageChangeListener(reversingListener);
    }

    @Override
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        ReversingOnPageChangeListener reverseListener = mPageChangeListeners.remove(listener);
        if (reverseListener != null) {
            super.removeOnPageChangeListener(reverseListener);
        }
    }

    @Override
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        mPageChangeListeners.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > height) {
                    height = h;
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public static class SavedState implements Parcelable {
        // The `CREATOR` field is used to create the parcelable from a parcel, even though it is never referenced directly.
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        private final Parcelable mViewPagerSavedState;
        private final int mLayoutDirection;

        private SavedState(Parcelable viewPagerSavedState, int layoutDirection) {
            mViewPagerSavedState = viewPagerSavedState;
            mLayoutDirection = layoutDirection;
        }

        private SavedState(Parcel in, ClassLoader loader) {
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            mViewPagerSavedState = in.readParcelable(loader);
            mLayoutDirection = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(mViewPagerSavedState, flags);
            out.writeInt(mLayoutDirection);
        }
    }

    private class ReversingOnPageChangeListener implements OnPageChangeListener {
        private final OnPageChangeListener mListener;

        public ReversingOnPageChangeListener(OnPageChangeListener listener) {
            mListener = listener;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // The documentation says that `getPageWidth(...)` returns the fraction of the _measured_ width that that page takes up.  However, the code seems to
            // use the width so we will here too.
            final int width = getWidth();
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (isRtl() && adapter != null) {
                int count = adapter.getCount();
                int remainingWidth = (int) (width * (1 - adapter.getPageWidth(position))) + positionOffsetPixels;
                while (position < count && remainingWidth > 0) {
                    position += 1;
                    remainingWidth -= (int) (width * adapter.getPageWidth(position));
                }
                position = count - position - 1;
                positionOffsetPixels = -remainingWidth;
                positionOffset = positionOffsetPixels / (width * adapter.getPageWidth(position));
            }
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (isRtl() && adapter != null) {
                position = adapter.getCount() - position - 1;
            }
            mListener.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    private class ReversingAdapter extends DelegatingPagerAdapter {
        public ReversingAdapter(@NonNull PagerAdapter adapter) {
            super(adapter);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (isRtl()) {
                position = getCount() - position - 1;
            }
            super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            int position = super.getItemPosition(object);
            if (isRtl()) {
                if (position == POSITION_UNCHANGED || position == POSITION_NONE) {
                    // We can't accept POSITION_UNCHANGED when in RTL mode because adding items to the end of the collection adds them to the beginning of the
                    // ViewPager.  Items whose positions do not change from the perspective of the wrapped adapter actually do change from the perspective of
                    // the ViewPager.
                    position = POSITION_NONE;
                } else {
                    position = getCount() - position - 1;
                }
            }
            return position;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (isRtl()) {
                position = getCount() - position - 1;
            }
            return super.getPageTitle(position);
        }

        @Override
        public float getPageWidth(int position) {
            if (isRtl()) {
                position = getCount() - position - 1;
            }
            return super.getPageWidth(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (isRtl()) {
                position = getCount() - position - 1;
            }
            return super.instantiateItem(container, position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (isRtl()) {
                position = getCount() - position - 1;
            }
            super.setPrimaryItem(container, position, object);
        }
    }
}
