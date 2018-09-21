package com.byagowi.persiancalendar.di;

import android.widget.Toast;

import com.byagowi.persiancalendar.MainApplication;

import javax.inject.Inject;

@PerChildFragment
public final class ChildFragmentDependency {
    @Inject
    public ChildFragmentDependency(MainApplication app) {
        Toast.makeText(app, "ChildFragmentDependency", Toast.LENGTH_SHORT).show();
    }
}
