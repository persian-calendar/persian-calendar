package com.byagowi.persiancalendar.checks;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

public class DiscourageRelativeLayoutTest extends LintDetectorTest {
    public void testBasic() {
        lint().files(
                java("RelativeLayout"))
                .run()
                .expect("src/test/pkg/TestClass1.java:5: Warning: This code mentions lint: Congratulations [ShortUniqueId]\n" +
                        "    private static String s2 = \"Let's say it: lint\";\n" +
                        "                               ~~~~~~~~~~~~~~~~~~~~\n" +
                        "0 errors, 1 warnings\n");
    }

    @Override
    protected Detector getDetector() {
        return new DiscourageRelativeLayout();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(DiscourageRelativeLayout.ISSUE);
    }
}