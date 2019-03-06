package com.byagowi.persiancalendar.calendar;

import com.byagowi.persiancalendar.calendar.persian.PersianLegacyConverter;
import com.byagowi.persiancalendar.calendar.persian.PersianLookupTableConverter;

public class PersianDate extends AbstractDate {

    public PersianDate(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }

    public PersianDate(long jdn) {
        super(jdn);
    }

    public PersianDate(AbstractDate date) {
        super(date);
    }

    // Converters
    @Override
    public long toJdn() {
        long result = PersianLookupTableConverter.toJdn(getYear(), getMonth(), getDayOfMonth());
        return result == -1 ? PersianLegacyConverter.toJdn(getYear(), getMonth(), getDayOfMonth()) : result;
    }

    @Override
    protected int[] fromJdn(long jdn) {
        int[] result = PersianLookupTableConverter.fromJdn(jdn);
        return result == null ? PersianLegacyConverter.fromJdn(jdn) : result;
    }
}
