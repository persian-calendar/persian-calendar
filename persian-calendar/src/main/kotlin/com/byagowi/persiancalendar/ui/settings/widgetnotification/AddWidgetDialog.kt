package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.variants.debugAssertNotNull

@Composable
fun AddWidgetDialog(closeDialog: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    AppDialog(onDismissRequest = closeDialog) {
        val context = LocalContext.current
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgets = runCatching {
            widgetManager.getInstalledProvidersForPackage(context.packageName, null)
        }.debugAssertNotNull.getOrNull() ?: emptyList()
        widgets.forEach { widget ->
            fun addWidget() {
                closeDialog()
                runCatching {
                    widgetManager.requestPinAppWidget(widget.provider, null, null)
                }.getOrNull().debugAssertNotNull
            }
            Spacer(Modifier.height(16.dp))
            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                widget.loadDescription(context).toString()
            } else stringResource(R.string.pref_widget)
            Image(
                bitmap = ImageBitmap.imageResource(widget.previewImage),
                contentDescription = description,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = ::addWidget),
            )
            TextButton(
                onClick = ::addWidget,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text(stringResource(R.string.add)) }
        }
        Spacer(Modifier.height(16.dp))
    }
}
