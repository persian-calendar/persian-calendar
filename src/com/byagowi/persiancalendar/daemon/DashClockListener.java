package com.byagowi.persiancalendar.daemon;

import com.google.android.apps.dashclock.api.DashClockExtension;

public class DashClockListener extends DashClockExtension {
	private final UpdateUtils updateUtils = UpdateUtils.getInstance();

	@Override
	protected void onUpdateData(int reason) {
		setUpdateWhenScreenOn(true);
		publishUpdate(updateUtils.getDashClockUpdatedData(this));
	}

}
