package com.byagowi.persiancalendar.ui

import android.view.View
import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BasicSmokeTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun basicSmokeTest() {
//        val appCompatImageButton = onView(
//            allOf(
//                withContentDescription("باز کردن کشوی پیمایش"), childAtPosition(
//                    allOf(
//                        withId(R.id.toolbar), childAtPosition(withId(R.id.app_bar), 0)
//                    ), 1
//                ), isDisplayed()
//            )
//        )
//        appCompatImageButton.perform(click())

//        onView(
//            allOf(
//                withId(R.id.exit), childAtPosition(
//                    allOf(
//                        withId(R.id.design_navigation_view),
//                        childAtPosition(withId(R.id.navigation), 0)
//                    ), 6
//                ), isDisplayed()
//            )
//        ).perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
        }
    }
}
