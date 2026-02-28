package com.byagowi.persiancalendar.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toSvg
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.ui.common.fillDrawerArrowPath
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawerArrowDrawableTest {
    @Test
    fun drawerMenuIcon() {
        assertEquals(
            "M3.0 18.0L21.0 18.0 21.0 16.0 3.0 16.0 3.0 18.0ZM3.0 13.0L21.0 13.0 21.0 11.0 3.0 11.0 3.0 13.0ZM3.0 6.0L3.0 8.0 21.0 8.0 21.0 6.0 3.0 6.0Z",
            ((Icons.Default.Menu.root[0] as? VectorPath)?.pathData?.toPath()
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
            "M20.0 11.0L7.83 11.0 13.42 5.41 12.0 4.0 4.0 12.0 12.0 20.0 13.41 18.59 7.83 13.0 20.0 13.0 20.0 11.0Z",
            ((Icons.AutoMirrored.Default.ArrowBack.root[0] as? VectorPath)?.pathData?.toPath()
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
