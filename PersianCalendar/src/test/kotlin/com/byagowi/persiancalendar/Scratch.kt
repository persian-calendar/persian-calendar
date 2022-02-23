package com.byagowi.persiancalendar

import org.junit.Assert.assertTrue
import org.junit.Test

//import okhttp3.HttpUrl
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONArray
//import org.json.JSONObject
//import org.junit.Test
//import java.net.URL
//import java.text.DateFormatSymbols
//import java.util.*

class Scratch {
    @Test
    fun test() {
        assertTrue(true)
    }

//    @Test
//    fun listBugs() {
//        fun JSONArray.toObjectList() = (0 until length()).map { getJSONObject(it) }
//
//        val issuesList = generateSequence(emptyList<JSONArray>()) { pages ->
//            runCatching {
//                val url = "https://api.github.com/repos/persian-calendar/persian-calendar/issues?state=all&per_page=100&page=${pages.size + 1}"
//                JSONArray(URL(url).readText()).takeIf { it.length() != 0 }?.let(pages::plusElement)
//            }.onFailure { it.printStackTrace() }.getOrNull()
//        }.last().fold(emptyList<JSONObject>()) { r, t -> r + t.toObjectList() }
//
//        println(issuesList.size)
//    }
//
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
//    private val httpClient = OkHttpClient.Builder().addNetworkInterceptor {
//        it.proceed(it.request()).also { response ->
//            val url = response.request().url()
//            println(
//                if (response.isSuccessful) "Successful ${response.request().url()}"
//                else "Error $url: ${response.code()} ${response.message()}"
//            )
//        }
//    }.build()
//
//    private fun getEquivalentTitles(titles: List<String>, langCode: String): List<String> {
//        val response = JSONObject(
//            runCatching {
//                httpClient.newCall(Request.Builder().url(HttpUrl.Builder()
//                    .scheme("https")
//                    .host("linkstranslator.toolforge.org")
//                    .addQueryParameter("from", "fa")
//                    .addQueryParameter("to", langCode)
//                    .addQueryParameter("p", titles.joinToString("|"))
//                    .build().url()).build()).execute().body()?.string()
//            }.onFailure { it.printStackTrace() }.getOrNull() ?: "{}"
//        )
//        return titles.map { response.optString(it, "") }
//    }
}
