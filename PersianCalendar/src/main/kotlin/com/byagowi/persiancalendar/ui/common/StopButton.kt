package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.MorphPolygonShape
import androidx.compose.foundation.shape.PolygonShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.resume
import com.byagowi.persiancalendar.shared.generated.resources.stop
import org.jetbrains.compose.resources.stringResource

@Composable
fun StopButton(
    isStopped: Boolean,
    modifier: Modifier = Modifier,
    onIsStoppedChange: (Boolean) -> Unit,
) {
    val contentDescription = stringResource(if (isStopped) Res.string.resume else Res.string.stop)
    AppFloatingActionButton(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onClick = { onIsStoppedChange(!isStopped) },
    ) {
        val progress by animateFloatAsState(if (isStopped) 1f else 0f)
        Box(
            Modifier
                .size((12 + 4 * progress).dp)
                .clip(
                    remember {
                        MorphPolygonShape(
                            start = PolygonShape.rectangle(),
                            end = PolygonShape.regularPolygon(numVertices = 3),
                            progress = { progress },
                        )
                    },
                )
                .background(LocalContentColor.current),
        )
    }
}
