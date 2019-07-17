package com.duolingo.open.rtlviewpager;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class DelegatingPagerAdapter extends PagerAdapter {
    @NonNull
    private final PagerAdapter mDelegate;

    public DelegatingPagerAdapter(@NonNull final PagerAdapter delegate) {
        this.mDelegate = delegate;
        delegate.registerDataSetObserver(new MyDataSetObserver(this));
    }

    @NonNull
    public PagerAdapter getDelegate() {
        return mDelegate;
    }

    public int getCount() {
        return mDelegate.getCount();
    }

    public void startUpdate(ViewGroup container) {
        mDelegate.startUpdate(container);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        return mDelegate.instantiateItem(container, position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            mDelegate.destroyItem(container, position, object);
        } catch (Exception ignore) {
            // don't crash at least on weird cases
        }
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mDelegate.setPrimaryItem(container, position, object);
    }

    public void finishUpdate(ViewGroup container) {
        mDelegate.finishUpdate(container);
    }

    @Deprecated
    public void startUpdate(View container) {
        mDelegate.startUpdate(container);
    }

    @Deprecated
    public Object instantiateItem(View container, int position) {
        return mDelegate.instantiateItem(container, position);
    }

    @Deprecated
    public void destroyItem(View container, int position, Object object) {
        mDelegate.destroyItem(container, position, object);
    }

    @Deprecated
    public void setPrimaryItem(View container, int position, Object object) {
        mDelegate.setPrimaryItem(container, position, object);
    }

    @Deprecated
    public void finishUpdate(View container) {
        mDelegate.finishUpdate(container);
    }

    public boolean isViewFromObject(View view, Object object) {
        return mDelegate.isViewFromObject(view, object);
    }

    public Parcelable saveState() {
        return mDelegate.saveState();
    }

    public void restoreState(Parcelable state, ClassLoader loader) {
        mDelegate.restoreState(state, loader);
    }

    public int getItemPosition(Object object) {
        return mDelegate.getItemPosition(object);
    }

    public void notifyDataSetChanged() {
        mDelegate.notifyDataSetChanged();
    }

    void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDelegate.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDelegate.unregisterDataSetObserver(observer);
    }

    public CharSequence getPageTitle(int position) {
        return mDelegate.getPageTitle(position);
    }

    public float getPageWidth(int position) {
        return mDelegate.getPageWidth(position);
    }

    private static class MyDataSetObserver extends DataSetObserver {
        final DelegatingPagerAdapter mParent;

        private MyDataSetObserver(DelegatingPagerAdapter mParent) {
            this.mParent = mParent;
        }

        @Override
        public void onChanged() {
            if (mParent != null) {
                mParent.superNotifyDataSetChanged();
            }
        }

        @Override
        public void onInvalidated() {
            onChanged();
        }
    }
}
