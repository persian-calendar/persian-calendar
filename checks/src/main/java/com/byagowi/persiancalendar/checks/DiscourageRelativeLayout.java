package com.byagowi.persiancalendar.checks;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.UastScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.UastLiteralUtils;

import java.util.Collections;
import java.util.List;

public class DiscourageRelativeLayout extends Detector implements UastScanner {
    public static final Issue ISSUE = Issue.create(
            "NoRelativeLayout",
            "Discouraging RelativeLayout",
            "This check highlights discourages use of RelativeLayout on this project",
            Category.CORRECTNESS,
            6,
            Severity.WARNING,
            new Implementation(
                    DiscourageRelativeLayout.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(ULiteralExpression.class);
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitLiteralExpression(ULiteralExpression expression) {
                String string = UastLiteralUtils.getValueIfStringLiteral(expression);
                if (string == null) {
                    return;
                }

                if (string.contains("RelativeLayout")) {
                    context.report(ISSUE, expression, context.getLocation(expression),
                            "Don't use RelativeLayout, please!");
                }
            }
        };
    }
}