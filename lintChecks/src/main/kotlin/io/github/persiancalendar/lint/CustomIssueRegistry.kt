package io.github.persiancalendar.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class CustomIssueRegistry : IssueRegistry() {
    override val issues = listOf(
        DoubleBangDetector.ISSUE,
        DebugAssertNotNullSafeCallDetector.ISSUE,
        TryCatchDetector.ISSUE,
        UnsafeCastDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor = Vendor(
        vendorName = "Persian Calendar",
        feedbackUrl = "https://github.com/persian-calendar/persian-calendar",
        contact = "https://github.com/persian-calendar/persian-calendar"
    )
}
