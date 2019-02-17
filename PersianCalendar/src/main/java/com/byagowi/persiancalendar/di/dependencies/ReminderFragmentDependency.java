package com.byagowi.persiancalendar.di.dependencies;

import com.byagowi.persiancalendar.di.scopes.PerFragment;
import com.byagowi.persiancalendar.reminder.fragment.ReminderFragment;

import javax.inject.Inject;

@PerFragment
public final class ReminderFragmentDependency {
    @Inject
    ReminderFragment reminderFragment;

    @Inject
    public ReminderFragmentDependency() {
    }

    public ReminderFragment getReminderFragment() {
        return reminderFragment;
    }
}
