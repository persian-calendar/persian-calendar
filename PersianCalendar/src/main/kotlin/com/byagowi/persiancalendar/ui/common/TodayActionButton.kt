package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.byagowi.persiancalendar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayActionButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = scaleIn(), exit = scaleOut()) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(text = stringResource(R.string.return_to_today)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_restore_modified),
                    contentDescription = stringResource(R.string.return_to_today),
                )
            }
        }
    }
}
