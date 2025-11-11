package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.animateColor

@Composable
fun ColumnScope.EncourageActionLayout(
    modifier: Modifier = Modifier,
    header: String,
    acceptButton: String = stringResource(R.string.settings),
    hideOnAccept: Boolean = true,
    discardAction: () -> Unit = {},
    acceptAction: () -> Unit,
) {
    var shown by rememberSaveable { mutableStateOf(true) }
    this.AnimatedVisibility(modifier = modifier, visible = shown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .semantics { this.isTraversalGroup = true }
        ) {
            Text(
                header,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Row {
                OutlinedButton(
                    onClick = {
                        discardAction()
                        shown = false
                    },
                    Modifier.weight(1f),
                ) { Text(stringResource(R.string.ignore)) }
                Spacer(Modifier.width(8.dp))
                val defaultButtonColors = ButtonDefaults.buttonColors()
                Button(
                    onClick = {
                        if (hideOnAccept) shown = false
                        acceptAction()
                    },
                    Modifier.weight(1f),
                    colors = defaultButtonColors.copy(
                        containerColor = animateColor(defaultButtonColors.containerColor).value,
                        contentColor = animateColor(defaultButtonColors.contentColor).value,
                        disabledContainerColor = animateColor(defaultButtonColors.disabledContainerColor).value,
                        disabledContentColor = animateColor(defaultButtonColors.disabledContentColor).value,
                    )
                ) { Text(acceptButton) }
            }
        }
    }
}
