package com.byagowi.persiancalendar.ui.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAstronomyBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.Eclipse
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*

class AstronomyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAstronomyBinding.inflate(layoutInflater)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.astronomical_info)
            it.setupUpNavigation()
        }

        val persianYear = Jdn.today().toPersianCalendar().year
        binding.headerInformation.text = (listOf(
            R.string.solar_eclipse to Eclipse.Category.SOLAR,
            R.string.lunar_eclipse to Eclipse.Category.LUNAR
        ).map { (title, eclipseCategory) ->
            val eclipse = Eclipse(GregorianCalendar(), eclipseCategory, true)
            val date = eclipse.maxPhaseDate.toJavaCalendar().formatDateAndTime()
            val type = eclipse.type.name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            getString(R.string.eclipse_of_type_in).format(getString(title), type, date)
        } + (1..4).map {
            val year = CivilDate(PersianDate(persianYear, it * 3, 29)).year
            when (it) {
                1 -> R.string.summer to Equinox.northernSolstice(year)
                2 -> R.string.fall to Equinox.southwardEquinox(year)
                3 -> R.string.winter to Equinox.southernSolstice(year)
                else -> R.string.spring to Equinox.northwardEquinox(year)
            }
        }.map { (season, equinox) ->
            getString(season) + spacedColon + equinox.toJavaCalendar().formatDateAndTime()
        }).joinToString("\n")

        fun update() {
            val date = GregorianCalendar().also {
                it.add(Calendar.MINUTE, (binding.slider.value * 24 * 60).toInt())
            }
            val sunMoonPosition = date.calculateSunMoonPosition(coordinates)
            binding.solarView.sunMoonPosition = sunMoonPosition
            binding.zodiac.text = listOf(
                date.formatDateAndTime(),
                /*☉*/getString(R.string.sun) + spacedColon +
                        sunMoonPosition.sunEcliptic.zodiac.format(binding.zodiac.context, true),
                /*☽*/getString(R.string.moon) + spacedColon +
                        sunMoonPosition.moonEcliptic.zodiac.format(binding.zodiac.context, true)
            ).joinToString("\n")
        }
        update()
        binding.slider.addOnChangeListener { _, _, _ -> update() }

        return binding.root
    }
}
