package com.byagowi.persiancalendar.ui.athan

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getPrayTimeImage
import com.byagowi.persiancalendar.utils.getPrayTimeName

fun setAthanActivityContent(activity: ComponentActivity, prayerKey: String, onClick: () -> Unit) {
    val cityName = activity.appPrefs.cityName
    activity.setContent { AthanActivityContent(prayerKey, cityName, onClick) }
}

@Composable
private fun AthanActivityContent(prayerKey: String, cityName: String?, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable { onClick() }) {
        Image(
            painter = painterResource(getPrayTimeImage(prayerKey)),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 80.dp)) {
            val textStyle = LocalTextStyle.current.copy(
                color = Color.White, fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 2f, offset = Offset(1f, 1f))
            )
            Text(stringResource(getPrayTimeName(prayerKey)), fontSize = 36.sp, style = textStyle)
            if (cityName != null) Text(
                stringResource(R.string.in_city_time, cityName),
                fontSize = 18.sp, style = textStyle, modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Preview
@Composable
private fun AthanActivityContentPreview() = AthanActivityContent(FAJR_KEY, "CITY NAME") {}
