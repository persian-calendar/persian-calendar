package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.PathParser
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

class MapFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // XXX: do it in a separate thread, don't show for low memory devices
        val zippedMapPath = resources.openRawResource(R.raw.worldmap).use { it.readBytes() }
        val mapPathString = String(GZIPInputStream(ByteArrayInputStream(zippedMapPath)).readBytes())
        val mapPath = PathParser.createPathFromPathData(mapPathString)
        val bitmap = Bitmap.createBitmap(4378, 2435, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also { it.drawPath(mapPath, Paint().apply { color = 0xffbcbcbc.toInt() }) }
        return ImageView(context).also { it.setImageDrawable(BitmapDrawable(resources, bitmap)) }
    }
}
