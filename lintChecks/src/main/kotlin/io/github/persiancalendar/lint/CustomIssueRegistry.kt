package io.github.persiancalendar.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class CustomIssueRegistry : IssueRegistry() {
    override val issues = listOf(
        FragileKotlinUsageDetector.DOUBLE_BANG_ISSUE,
        FragileKotlinUsageDetector.ERROR_FUNCTION_ISSUE,
        FragileKotlinUsageDetector.THROW_ISSUE,
        SuppressLintDetector.ISSUE,
        ElvisEmptyStringDetector.ISSUE,
        DebugAssertNotNullSafeCallDetector.ISSUE,
        TryCatchDetector.ISSUE,
        UnsafeCastDetector.ISSUE,
        HapticFeedbackDetector.ISSUE,
        BackHandlerDetector.ISSUE,
        ComposeStringResourceDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor = Vendor(
        vendorName = "Persian Calendar",
        feedbackUrl = "https://github.com/persian-calendar/persian-calendar",
        contact = "https://github.com/persian-calendar/persian-calendar",
    )
}
