package com.byagowi.persiancalendar.ui.converter

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds
