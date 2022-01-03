package com.byagowi.persiancalendar.ui.utils

import android.graphics.Path
import androidx.core.graphics.PathParser
import com.byagowi.persiancalendar.BuildConfig

class MorphedPath(fromPath: String, toPath: String) {

    val path = Path()

    private val nodesFrom = PathParser.createNodesFromPathData(fromPath)
    private val currentNodes = PathParser.deepCopyNodes(nodesFrom)
    private val nodesTo = PathParser.createNodesFromPathData(toPath)

    init {
        if (BuildConfig.DEVELOPMENT) check(PathParser.canMorph(nodesFrom, nodesTo))
    }

    fun interpolateTo(fraction: Float) {
        PathParser.interpolatePathDataNodes(currentNodes, nodesFrom, nodesTo, fraction)
        path.rewind()
        PathParser.PathDataNode.nodesToPath(currentNodes, path)
    }
}
