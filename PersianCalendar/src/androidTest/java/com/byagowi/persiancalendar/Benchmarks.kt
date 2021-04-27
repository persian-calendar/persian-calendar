package com.byagowi.persiancalendar

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.utils.loadEvents
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Benchmarks {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkLoadEvents() = benchmarkRule.measureRepeated {
        loadEvents(ApplicationProvider.getApplicationContext())
    }
}