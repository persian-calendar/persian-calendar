package com.byagowi.persiancalendar.ui.map

import android.graphics.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes

/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This has an interesting history in this project, initially we were using
 * [androidx.core.graphics.PathParser.createPathFromPathData()] but after a
 * version upgrade turned out the function wasn't supposed to be public, that's why
 * we had to copy it's implementation to this project to be able to use it.
 *
 * But now after having Compose in the project turned out
 * [androidx.compose.ui.graphics.vector.addPathNodes] or
 * [androidx.compose.ui.graphics.vector.PathParser.parsePathString] is public
 * but the catch is it it's toPath() returns a Compose Path instead of Android
 * native one which as we use this in widget it's not we exactly want so implementation
 * of [androidx.compose.ui.graphics.vector.toPath] is copied but changed to return
 * an Android native path instead of Compose one.
 *
 * If PathParser of Compose also becomes private in future however we can revert the
 * change and use the older version again.
 *
 * Note: Arc command support is removed.
 */
fun createPathFromPathData(pathData: String): Path {
    val nodes: List<PathNode> = addPathNodes(pathData)
    val target = Path()
    var currentX = 0.0f
    var currentY = 0.0f
    var ctrlX = 0.0f
    var ctrlY = 0.0f
    var segmentX = 0.0f
    var segmentY = 0.0f
    var reflectiveCtrlX: Float
    var reflectiveCtrlY: Float

    var previousNode = if (nodes.isEmpty()) PathNode.Close else nodes[0]
    nodes.forEach { node ->
        when (node) {
            is PathNode.Close -> {
                currentX = segmentX
                currentY = segmentY
                ctrlX = segmentX
                ctrlY = segmentY
                target.close()
                target.moveTo(currentX, currentY)
            }

            is PathNode.RelativeMoveTo -> {
                currentX += node.dx
                currentY += node.dy
                target.rMoveTo(node.dx, node.dy)
                segmentX = currentX
                segmentY = currentY
            }

            is PathNode.MoveTo -> {
                currentX = node.x
                currentY = node.y
                target.moveTo(node.x, node.y)
                segmentX = currentX
                segmentY = currentY
            }

            is PathNode.RelativeLineTo -> {
                target.rLineTo(node.dx, node.dy)
                currentX += node.dx
                currentY += node.dy
            }

            is PathNode.LineTo -> {
                target.lineTo(node.x, node.y)
                currentX = node.x
                currentY = node.y
            }

            is PathNode.RelativeHorizontalTo -> {
                target.rLineTo(node.dx, 0.0f)
                currentX += node.dx
            }

            is PathNode.HorizontalTo -> {
                target.lineTo(node.x, currentY)
                currentX = node.x
            }

            is PathNode.RelativeVerticalTo -> {
                target.rLineTo(0.0f, node.dy)
                currentY += node.dy
            }

            is PathNode.VerticalTo -> {
                target.lineTo(currentX, node.y)
                currentY = node.y
            }

            is PathNode.RelativeCurveTo -> {
                target.rCubicTo(
                    node.dx1, node.dy1,
                    node.dx2, node.dy2,
                    node.dx3, node.dy3
                )
                ctrlX = currentX + node.dx2
                ctrlY = currentY + node.dy2
                currentX += node.dx3
                currentY += node.dy3
            }

            is PathNode.CurveTo -> {
                target.cubicTo(
                    node.x1, node.y1,
                    node.x2, node.y2,
                    node.x3, node.y3
                )
                ctrlX = node.x2
                ctrlY = node.y2
                currentX = node.x3
                currentY = node.y3
            }

            is PathNode.RelativeReflectiveCurveTo -> {
                if (previousNode.isCurve) {
                    reflectiveCtrlX = currentX - ctrlX
                    reflectiveCtrlY = currentY - ctrlY
                } else {
                    reflectiveCtrlX = 0.0f
                    reflectiveCtrlY = 0.0f
                }
                target.rCubicTo(
                    reflectiveCtrlX, reflectiveCtrlY,
                    node.dx1, node.dy1,
                    node.dx2, node.dy2
                )
                ctrlX = currentX + node.dx1
                ctrlY = currentY + node.dy1
                currentX += node.dx2
                currentY += node.dy2
            }

            is PathNode.ReflectiveCurveTo -> {
                if (previousNode.isCurve) {
                    reflectiveCtrlX = 2 * currentX - ctrlX
                    reflectiveCtrlY = 2 * currentY - ctrlY
                } else {
                    reflectiveCtrlX = currentX
                    reflectiveCtrlY = currentY
                }
                target.cubicTo(
                    reflectiveCtrlX, reflectiveCtrlY,
                    node.x1, node.y1, node.x2, node.y2
                )
                ctrlX = node.x1
                ctrlY = node.y1
                currentX = node.x2
                currentY = node.y2
            }

            is PathNode.RelativeQuadTo -> {
                target.rQuadTo(node.dx1, node.dy1, node.dx2, node.dy2)
                ctrlX = currentX + node.dx1
                ctrlY = currentY + node.dy1
                currentX += node.dx2
                currentY += node.dy2
            }

            is PathNode.QuadTo -> {
                target.quadTo(node.x1, node.y1, node.x2, node.y2)
                ctrlX = node.x1
                ctrlY = node.y1
                currentX = node.x2
                currentY = node.y2
            }

            is PathNode.RelativeReflectiveQuadTo -> {
                if (previousNode.isQuad) {
                    reflectiveCtrlX = currentX - ctrlX
                    reflectiveCtrlY = currentY - ctrlY
                } else {
                    reflectiveCtrlX = 0.0f
                    reflectiveCtrlY = 0.0f
                }
                target.rQuadTo(
                    reflectiveCtrlX,
                    reflectiveCtrlY, node.dx, node.dy
                )
                ctrlX = currentX + reflectiveCtrlX
                ctrlY = currentY + reflectiveCtrlY
                currentX += node.dx
                currentY += node.dy
            }

            is PathNode.ReflectiveQuadTo -> {
                if (previousNode.isQuad) {
                    reflectiveCtrlX = 2 * currentX - ctrlX
                    reflectiveCtrlY = 2 * currentY - ctrlY
                } else {
                    reflectiveCtrlX = currentX
                    reflectiveCtrlY = currentY
                }
                target.quadTo(
                    reflectiveCtrlX,
                    reflectiveCtrlY, node.x, node.y
                )
                ctrlX = reflectiveCtrlX
                ctrlY = reflectiveCtrlY
                currentX = node.x
                currentY = node.y
            }

            // Not supported
            is PathNode.RelativeArcTo -> Unit
            is PathNode.ArcTo -> Unit
        }
        previousNode = node
    }
    return target
}
