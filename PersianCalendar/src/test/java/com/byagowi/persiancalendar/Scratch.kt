//package com.byagowi.persiancalendar
//
//import com.ibm.icu.text.DateFormatSymbols
//import com.squareup.okhttp.HttpUrl
//import org.json.JSONObject
//import org.junit.Test
//import java.util.*
//
//class Scratch {
//    @Test
//    fun getMonths() {
//        val langCode = "zh"
//        print(getEquivalentMonths(
//            DateFormatSymbols(Locale("fa-u-ca-persian")).months.toList(),
//            langCode
//        ).joinToString("\n") { "  \"$it\"," })
//        print("\n  ----\n")
//        print(getEquivalentMonths(
//            DateFormatSymbols(Locale("fa-u-ca-islamic")).months.toList(),
//            langCode
//        ).joinToString("\n") { "  \"$it\"," })
//        print("\n----\n")
//        print(getEquivalentMonths(
//            DateFormatSymbols(Locale("fa-u-ca-iso8601")).months.toList(),
//            langCode
//        ).joinToString("\n") { "  \"$it\"," })
//        print("\n----\n")
//        val weekdays = DateFormatSymbols(Locale(langCode)).weekdays.toList()
//        print(
//            (0..6).map { weekdays[if (it == 0) 7 else it] }
//                .joinToString("\n") { "  \"$it\"," }
//        )
//    }
//
//    private fun getEquivalentMonths(titles: List<String>, langCode: String): List<String> {
//        val normalized = titles.map { it.replace("ٔ", "") } // remove ـٔ
//        return getEquivalentTitles(normalized.map { "$it (ماه)" }, langCode).zip(
//            getEquivalentTitles(normalized, langCode)
//        ).map { (if (it.first.isNotEmpty()) it.first else it.second).split(" (")[0] }
//    }
//
//    private fun getEquivalentTitles(titles: List<String>, langCode: String): List<String> {
//        val response = JSONObject(
//            HttpUrl.Builder()
//                .scheme("https")
//                .host("linkstranslator.toolforge.org")
//                .addQueryParameter("from", "fa")
//                .addQueryParameter("to", langCode)
//                .addQueryParameter("p", titles.joinToString("|"))
//                .build().url().readText()
//        )
//        return titles.map { response.optString(it, "") }
//    }
//}