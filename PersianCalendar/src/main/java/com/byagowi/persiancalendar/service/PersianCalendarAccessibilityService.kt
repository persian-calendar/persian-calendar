package com.byagowi.persiancalendar.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class PersianCalendarAccessibilityService : AccessibilityService() {
    override fun onInterrupt() = Unit
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
}