package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import com.byagowi.persiancalendar.PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH
import com.byagowi.persiancalendar.PREF_LOCAL_NUMERAL
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.alternativePersianMonthsInAzeri
import com.byagowi.persiancalendar.global.easternGregorianArabicMonths
import com.byagowi.persiancalendar.global.englishGregorianPersianMonths
import com.byagowi.persiancalendar.global.englishWeekDaysInIranEnglish
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.localNumeralPreference
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun ColumnScope.InterfaceSettings(destination: String? = null) {
    val context = LocalContext.current
    run {
        val themeDisplayName = stringResource(run {
            val currentKey = context.preferences.getString(PREF_THEME, null)
            Theme.entries.firstOrNull { it.key == currentKey } ?: Theme.SYSTEM_DEFAULT
        }.title)
        Box(
            Modifier
                .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                .clearAndSetSemantics {},
        ) {
            SettingsClickable(
                title = stringResource(R.string.select_skin),
                summary = themeDisplayName,
                defaultOpen = destination == PREF_THEME,
            ) { onDismissRequest -> ThemeDialog(onDismissRequest) }
        }
    }
    val language by language.collectAsState()
    SettingsClickable(
        title = stringResource(R.string.language),
        summary = language.nativeName,
    ) { onDismissRequest -> LanguageDialog(onDismissRequest) }
    this.AnimatedVisibility(language.isPersian) {
        val englishGregorianPersianMonths by englishGregorianPersianMonths.collectAsState()
        SettingsSwitch(
            key = PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
            value = englishGregorianPersianMonths,
            title = "ماه‌های میلادی با نام انگلیسی",
            summary = "جون، جولای، آگوست، …"
        )
    }
    this.AnimatedVisibility(language.isArabic) {
        val easternGregorianArabicMonths by easternGregorianArabicMonths.collectAsState()
        SettingsSwitch(
            key = PREF_EASTERN_GREGORIAN_ARABIC_MONTHS,
            value = easternGregorianArabicMonths,
            title = "السنة الميلادية بالاسماء الشرقية",
            summary = "كانون الثاني، شباط، آذار، …"
        )
    }
    this.AnimatedVisibility(language == Language.AZB) {
        val alternativePersianMonthsInAzeri by alternativePersianMonthsInAzeri.collectAsState()
        SettingsSwitch(
            key = PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS,
            value = alternativePersianMonthsInAzeri,
            title = "آذربایجان دیلینده ایل آیلار",
            summary = "آغلارگۆلر، گۆلن، قیزاران، …"
        )
    }
    this.AnimatedVisibility(language == Language.EN_IR) {
        val englishWeekDaysInIranEnglish by englishWeekDaysInIranEnglish.collectAsState()
        SettingsSwitch(
            key = PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH,
            value = englishWeekDaysInIranEnglish,
            title = "English weekday names",
            summary = "Sunday, Monday, Tuesday, …"
        )
    }
    this.AnimatedVisibility(language.canHaveLocalNumeral) {
        val localNumeralPreference by localNumeralPreference.collectAsState()
        SettingsSwitch(
            key = PREF_LOCAL_NUMERAL,
            value = localNumeralPreference,
            title = stringResource(R.string.native_digits),
            summary = stringResource(R.string.enable_native_digits)
        )
    }
}
