package com.byagowi.persiancalendar.ui.calendar;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarFragmentModel extends ViewModel {
    public final MutableLiveData<MonthFragmentUpdateCommand> monthFragmentsHandler = new MutableLiveData<>();
    public final MutableLiveData<Long> selectedDayLiveData = new MutableLiveData<>();
    public boolean isTheFirstTime = true;

    void monthFragmentsUpdate(MonthFragmentUpdateCommand command) {
        monthFragmentsHandler.postValue(command);
    }

    public void selectDay(long jdn) {
        selectedDayLiveData.postValue(jdn);
    }

    static public class MonthFragmentUpdateCommand {
        final public int target;
        final public boolean isEventsModification;
        final public long currentlySelectedJdn;

        MonthFragmentUpdateCommand(int target, boolean isEventsModification, long currentSelectedJdn) {
            this.target = target;
            this.isEventsModification = isEventsModification;
            this.currentlySelectedJdn = currentSelectedJdn;
        }
    }
}
