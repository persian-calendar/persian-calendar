package com.byagowi.persiancalendar.ui

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toSvg
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.icons.material.filled.arrowBackIcon
import com.byagowi.persiancalendar.icons.material.filled.menuIcon
import com.byagowi.persiancalendar.ui.common.fillDrawerArrowPath
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawerArrowDrawableTest {
    // This doesn't do meaningful comparison between Compose provided Icons and drawer arrow right now
    // but at least is good to make sure neither of them have drifted over the time.
    @Test
    fun drawerMenuIcon() {
        assertEquals(
            "M3.0 18.0L3.0 16.0 21.0 16.0 21.0 18.0 3.0 18.0ZM3.0 13.0L3.0 11.0 21.0 11.0 21.0 13.0 3.0 13.0ZM3.0 8.0L3.0 6.0 21.0 6.0 21.0 8.0 3.0 8.0Z",
            ((menuIcon.root[0] as? VectorPath)?.pathData?.toPath()
                ?: return fail("Not a path data?")).toSvg(),
        )
        assertEquals(
            "M-9.0 0.0L9.0 0.0M-9.0 5.0L9.0 5.0M-9.0 -5.0L9.0 -5.0Z",
            Path().also {
                fillDrawerArrowPath(it, true, 0f, 1f, 2 * 1f)
            }.toSvg(),
        )
    }

    @Test
    fun arrowBackIcon() {
        assertEquals(
            "M7.83 13.0L13.43 18.6 12.0 20.0 4.0 12.0 12.0 4.0 13.43 5.4 7.83 11.0 20.0 11.0 20.0 13.0 7.83 13.0Z",
            ((arrowBackIcon.root[0] as? VectorPath)?.pathData?.toPath()
                ?: return fail("Not a path data?")).toSvg(),
        )
        assertEquals(
            "M-7.2928934 0.0L7.2928934 0.0M-8.0 -0.70710677L-4.7683716E-7 7.292893M-8.0 0.70710677L-4.7683716E-7 -7.292893Z",
            Path().also {
                fillDrawerArrowPath(it, true, 1f, 1f, 2 * 1f)
            }.toSvg(),
        )
    }
}
