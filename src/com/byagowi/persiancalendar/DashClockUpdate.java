package com.byagowi.persiancalendar;

import com.google.android.apps.dashclock.api.DashClockExtension;

public class DashClockUpdate extends DashClockExtension {
	private final UpdateUtils updateUtils = UpdateUtils.getInstance();

	@Override
	protected void onUpdateData(int reason) {
		setUpdateWhenScreenOn(true);
		updateUtils.update(this);
		publishUpdate(updateUtils.getExtensionData());
	}

}
