package com.byagowi.persiancalendar.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarFragmentModel extends ViewModel {
    static public class MonthFragmentUpdateCommand {
        final public int target;
        final public boolean isEventsModification;
        final public long currentlySelectedJdn;

        public MonthFragmentUpdateCommand(int target, boolean isEventsModification, long currentSelectedJdn) {
            this.target = target;
            this.isEventsModification = isEventsModification;
            this.currentlySelectedJdn = currentSelectedJdn;
        }
    }

    public final MutableLiveData<MonthFragmentUpdateCommand> monthFragmentsHandler = new MutableLiveData<>();

    public void monthFragmentsUpdate(MonthFragmentUpdateCommand command) {
        monthFragmentsHandler.postValue(command);
    }
}
