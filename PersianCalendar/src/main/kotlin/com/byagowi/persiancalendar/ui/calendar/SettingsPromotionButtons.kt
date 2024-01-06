package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun SettingsPromotionButtons(
    modifier: Modifier = Modifier,
    header: String,
    acceptButton: String = stringResource(R.string.settings),
    discardAction: () -> Unit = {},
    acceptAction: () -> Unit,
) {
    var shown by rememberSaveable { mutableStateOf(true) }
    AnimatedVisibility(modifier = modifier, visible = shown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Text(
                header,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                OutlinedButton(
                    onClick = {
                        discardAction()
                        shown = false
                    },
                    Modifier.weight(1f),
                ) { Text(stringResource(R.string.ignore)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        shown = false
                        acceptAction()
                    },
                    Modifier.weight(1f),
                ) { Text(acceptButton) }
            }
        }
    }
}
