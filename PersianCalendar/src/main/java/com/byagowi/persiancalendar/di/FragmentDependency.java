package com.byagowi.persiancalendar.di;

import android.widget.Toast;

import com.byagowi.persiancalendar.MainApplication;

import javax.inject.Inject;

@PerFragment
public final class FragmentDependency {
    @Inject
    public FragmentDependency(MainApplication app) {
        Toast.makeText(app, "FragmentDependency", Toast.LENGTH_SHORT).show();
    }
}
