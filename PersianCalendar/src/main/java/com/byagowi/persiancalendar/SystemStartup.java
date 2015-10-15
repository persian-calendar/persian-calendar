package com.byagowi.persiancalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class SystemStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utils utils = Utils.getInstance();

        context.startService(new Intent(context, ApplicationService.class));
        utils.setAthanRepeater(context);
    }
}
