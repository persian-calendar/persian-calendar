package com.byagowi.persiancalendar.util;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {
    public static final AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
    }
}
