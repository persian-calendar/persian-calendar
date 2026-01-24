package com.byagowi.persiancalendar.utils

import android.content.res.Resources
import android.icu.util.ChineseCalendar
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import io.github.cosinekitty.astronomy.sunPosition
import java.util.GregorianCalendar
import kotlin.math.floor

private val lunarMonths = listOf(
    "正月",
    "二月",
    "三月",
    "四月",
    "五月",
    "六月",
    "七月",
    "八月",
    "九月",
    "十月",
    "冬月",
    "腊月",
)

private val lunarDays = listOf(
    "初一",
    "初二",
    "初三",
    "初四",
    "初五",
    "初六",
    "初七",
    "初八",
    "初九",
    "初十",
    "十一",
    "十二",
    "十三",
    "十四",
    "十五",
    "十六",
    "十七",
    "十八",
    "十九",
    "二十",
    "廿一",
    "廿二",
    "廿三",
    "廿四",
    "廿五",
    "廿六",
    "廿七",
    "廿八",
    "廿九",
    "三十",
)

private val heavenlyStems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
private val earthlyBranches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

private val solarTerms = listOf(
    "立春",
    "雨水",
    "惊蛰",
    "春分",
    "清明",
    "谷雨",
    "立夏",
    "小满",
    "芒种",
    "夏至",
    "小暑",
    "大暑",
    "立秋",
    "处暑",
    "白露",
    "秋分",
    "寒露",
    "霜降",
    "立冬",
    "小雪",
    "大雪",
    "冬至",
    "小寒",
    "大寒",
)

private val lunarFestivals = mapOf(
    1 to mapOf(
        1 to "春节",
        15 to "元宵节",
    ),
    2 to mapOf(
        2 to "龙抬头",
    ),
    3 to mapOf(
        3 to "上巳节",
    ),
    5 to mapOf(
        5 to "端午节",
    ),
    7 to mapOf(
        7 to "七夕",
        15 to "中元节",
    ),
    8 to mapOf(
        15 to "中秋节",
    ),
    9 to mapOf(
        9 to "重阳节",
    ),
    10 to mapOf(
        15 to "下元节",
    ),
    12 to mapOf(
        8 to "腊八节",
        23 to "小年",
        24 to "小年",
    ),
)

private data class LunarDate(val year: Int, val month: Int, val day: Int, val isLeapMonth: Boolean)

fun buildChineseLunarLines(resources: Resources, jdn: Jdn, nowMillis: Long): List<String> {
    val chineseCalendar = ChineseCalendar(jdn.toGregorianCalendar().time)
    val lunarDate = chineseCalendar.toLunarDate()
    val dateLabel = resources.getString(
        R.string.chinese_lunar_date,
        lunarDate.formatDate(),
    )
    val chineseZodiac = ChineseZodiac.fromChineseCalendar(chineseCalendar)
    val zodiac = listOfNotNull(
        chineseZodiac.resolveEmoji(false).takeIf { it.isNotBlank() },
        chineseZodiac.format(resources, withEmoji = false, isPersian = false),
    ).joinToString(" ")
    val zodiacLabel = resources.getString(R.string.chinese_zodiac, zodiac)

    val ganzhi = chineseCalendar.toBaZi(jdn, nowMillis)
    val baziLabel = resources.getString(R.string.eight_characters, ganzhi)

    val shichenLabel = resources.getString(
        R.string.time_branch,
        chineseCalendar.shichen(nowMillis),
    )

    val solarTerm = chineseCalendar.currentSolarTerm(jdn)
    val solarTermLabel = solarTerm?.let { resources.getString(R.string.solar_term, it) }

    val festivals = chineseCalendar.festivalsFor(lunarDate)
    val festivalsLabel = festivals.takeIf { it.isNotEmpty() }?.let {
        resources.getString(R.string.traditional_festivals, it.joinToString("、"))
    }

    return listOfNotNull(
        dateLabel,
        zodiacLabel,
        baziLabel,
        shichenLabel,
        solarTermLabel,
        festivalsLabel,
    )
}

private fun ChineseCalendar.toLunarDate(): LunarDate {
    val year = this[ChineseCalendar.YEAR]
    val month = this[ChineseCalendar.MONTH] + 1
    val day = this[ChineseCalendar.DAY_OF_MONTH]
    val isLeapMonth = this[ChineseCalendar.IS_LEAP_MONTH] == 1
    return LunarDate(year, month, day, isLeapMonth)
}

private fun LunarDate.formatDate(): String {
    val monthName = lunarMonths.getOrNull(month - 1) ?: "${month}月"
    val dayName = lunarDays.getOrNull(day - 1) ?: day.toString()
    return (if (isLeapMonth) "闰" else "") + monthName + dayName
}

private fun ChineseCalendar.toBaZi(jdn: Jdn, nowMillis: Long): String {
    val yearStemIndex = (this[ChineseCalendar.YEAR] - 4).mod(10)
    val yearBranchIndex = (this[ChineseCalendar.YEAR] - 4).mod(12)
    val yearGanzhi = heavenlyStems[yearStemIndex] + earthlyBranches[yearBranchIndex]

    val lunarMonth = this[ChineseCalendar.MONTH] + 1
    val monthBranchIndex = (lunarMonth + 1).mod(12)
    val monthStemIndex = (yearStemIndex * 2 + lunarMonth + 9).mod(10)
    val monthGanzhi = heavenlyStems[monthStemIndex] + earthlyBranches[monthBranchIndex]

    val dayIndex = (jdn.value + 49).mod(60).toInt()
    val dayStemIndex = dayIndex.mod(10)
    val dayBranchIndex = dayIndex.mod(12)
    val dayGanzhi = heavenlyStems[dayStemIndex] + earthlyBranches[dayBranchIndex]

    val hourBranchIndex = hourBranchIndex(nowMillis)
    val hourStemIndex = (dayStemIndex * 2 + hourBranchIndex).mod(10)
    val hourGanzhi = heavenlyStems[hourStemIndex] + earthlyBranches[hourBranchIndex]

    return listOf(yearGanzhi, monthGanzhi, dayGanzhi, hourGanzhi).joinToString(" ")
}

private fun ChineseCalendar.shichen(nowMillis: Long): String {
    val branchIndex = hourBranchIndex(nowMillis)
    val branch = earthlyBranches[branchIndex]
    val range = when (branchIndex) {
        0 -> "23-1"
        1 -> "1-3"
        2 -> "3-5"
        3 -> "5-7"
        4 -> "7-9"
        5 -> "9-11"
        6 -> "11-13"
        7 -> "13-15"
        8 -> "15-17"
        9 -> "17-19"
        10 -> "19-21"
        else -> "21-23"
    }
    return "${branch}时($range)"
}

private fun hourBranchIndex(nowMillis: Long): Int {
    val hour = GregorianCalendar().also { it.timeInMillis = nowMillis }[GregorianCalendar.HOUR_OF_DAY]
    return ((hour + 1) / 2).mod(12)
}

private fun ChineseCalendar.currentSolarTerm(jdn: Jdn): String? {
    val time = jdn.toAstronomyTime(hourOfDay = 12)
    val solarLongitude = sunPosition(time).elon.mod(360.0)
    val offset = (solarLongitude - 315.0 + 360.0).mod(360.0)
    val index = floor(offset / 15.0).toInt().coerceIn(0, solarTerms.lastIndex)
    return solarTerms.getOrNull(index)
}

private fun ChineseCalendar.festivalsFor(lunarDate: LunarDate): List<String> {
    val festivals = mutableListOf<String>()
    lunarFestivals[lunarDate.month]?.get(lunarDate.day)?.let { festivals.add(it) }

    val monthLength = this.getActualMaximum(ChineseCalendar.DAY_OF_MONTH)
    if (lunarDate.month == 12 && lunarDate.day == monthLength) {
        festivals.add("除夕")
    }

    return festivals
}
