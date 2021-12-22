package com.byagowi.persiancalendar.ui.compass

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAstronomyBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.shared.ArrowView
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.Eclipse
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*
import kotlin.math.abs

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

        setupSlider(binding)

        return binding.root
    }

    private fun setupSlider(binding: FragmentAstronomyBinding) {
        fun update(offset: Int, immediate: Boolean) {
            val time = GregorianCalendar().also { it.add(Calendar.SECOND, offset) }
            binding.solarView.setTime(time, immediate) {
                binding.zodiac.text = listOf(
                    time.formatDateAndTime(),
                    /*☉*/getString(R.string.sun) + spacedColon +
                            it.sunEcliptic.zodiac.format(binding.zodiac.context, true),
                    /*☽*/getString(R.string.moon) + spacedColon +
                            it.moonEcliptic.zodiac.format(binding.zodiac.context, true)
                ).joinToString("\n")
            }
        }
        update(0, true)

        val slider = binding.slider
        slider.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(layoutInflater.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        val size = 500000
        slider.layoutManager = layoutManager
        slider.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.strokeWidth = 1.5.dp
                it.color = 0x80808080.toInt()
            }
            private val commonLayoutParams = ViewGroup.LayoutParams(10.dp.toInt(), 45.dp.toInt())

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
            override fun getItemCount() = size
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(object : View(parent.context) {
                    init {
                        layoutParams = commonLayoutParams
                    }

                    override fun onDraw(canvas: Canvas?) {
                        canvas?.drawLine(width / 2f, 0f, width / 2f, height / 1f, paint)
                    }
                }) {}
        }
        slider.scrollToPosition(size / 2)

        var velocity = 0
        // Maybe LinearSnapHelper().attachToRecyclerView(slider) and override its onFling?
        slider.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                velocity = velocityX
                return false
            }
        }

        var offset = 0

        val resetButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.isVisible = false
        }

        resetButton.onClick {
            offset = 0
            resetButton.isVisible = false
            update(offset, false)
        }

        val viewDirection = if (resources.isRtl) -1 else 1

        var disableSliderUpdates = false
        slider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (disableSliderUpdates) return
                offset += dx * abs(velocity) / 500 * viewDirection
                resetButton.isVisible = offset != 0
                update(offset, true)
            }
        })

        fun buttonScrollSlider(days: Int): Boolean {
            disableSliderUpdates = true
            binding.root.postDelayed({ disableSliderUpdates = false }, 1000)
            slider.smoothScrollBy(10 * days * viewDirection, 0)
            offset -= days * 60 * 60 * 24
            update(offset, false)
            return true
        }
        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener { buttonScrollSlider(1) }
        binding.startArrow.setOnLongClickListener { buttonScrollSlider(365) }
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener { buttonScrollSlider(-1) }
        binding.endArrow.setOnLongClickListener { buttonScrollSlider(-365) }
    }
}
