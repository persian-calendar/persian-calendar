package com.byagowi.persiancalendar.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.shared.ComposeFragment

class LicensesFragment : ComposeFragment() {
    override val isUpNavigation: Boolean get() = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val sections = remember { context.resources.getCreditsSections() }
        title.value = stringResource(R.string.about_license_title)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            LazyColumn {
                items(sections) { (title, license, text) ->
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = LocalTextStyle.current)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (license != null) Text(
                            license,
                            modifier = Modifier
                                .background(Color.LightGray, RoundedCornerShape(CornerSize(3.dp)))
                                .padding(horizontal = 4.dp),
                            style = TextStyle(Color.DarkGray, 12.sp)
                        )
                    }
                    Text(text)
                    Divider(color = Color.LightGray)
                }
            }
        }
    }
}
