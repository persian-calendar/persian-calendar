package com.byagowi.persiancalendar.ui.settings.locationathan.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.generated.districtsStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveLocation
import io.github.persiancalendar.praytimes.Coordinates

@Composable
fun ProvincesDialog(onDismissRequest: () -> Unit) {
    var province by rememberSaveable { mutableStateOf<String?>(null) }
    if (province != null) return DistrictsDialog(province ?: "", onDismissRequest)
    Dialog(
        title = { Text("انتخاب استان برای مشاهدهٔ بخش‌ها") },
        onDismissRequest = onDismissRequest,
    ) {
        districtsStore.forEach { (provinceName, _) ->
            Text(
                provinceName,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { province = provinceName }
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun DistrictsDialog(province: String, onDismissRequest: () -> Unit) {
    val districts = remember {
        (districtsStore[province] ?: emptyList()).flatMap { county ->
            val countyDetails = county.split(";")
            countyDetails.drop(1).map { it.split(":") to countyDetails[0] }
        }.sortedBy { (district, _) -> language.prepareForSort(district[0/*district name*/]) }
    }
    Dialog(
        title = { Text(province) },
        onDismissRequest = onDismissRequest,
    ) {
        val context = LocalContext.current
        districts.forEachIndexed { index, (district, county) ->
            Text(
                buildAnnotatedString {
                    withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                        append(district[0])
                    }
                    append(" ")
                    withStyle(
                        MaterialTheme.typography.bodyMedium.copy(
                            color = LocalTextStyle.current.color.copy(.5f)
                        ).toSpanStyle()
                    ) { append(county) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        val coordinates = Coordinates(
                            districts[index].first[1/*latitude*/].toDoubleOrNull() ?: 0.0,
                            districts[index].first[2/*longitude*/].toDoubleOrNull() ?: 0.0,
                            0.0,
                        )
                        context.appPrefs.saveLocation(coordinates, districts[index].first[0])
                    }
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            )
        }
    }
}
