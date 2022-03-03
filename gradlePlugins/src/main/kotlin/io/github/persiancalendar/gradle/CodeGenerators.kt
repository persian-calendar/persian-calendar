package io.github.persiancalendar.gradle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.typeNameOf
import com.squareup.kotlinpoet.withIndent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

abstract class CodeGenerators : DefaultTask() {

    private val packageName = "com.byagowi.persiancalendar.generated"

    private val calendarRecordName = "CalendarRecord"
    private val eventTypeName = "EventType"
    private val cityItemName = "CityItem"

    private val calendarRecordType = ClassName(packageName, calendarRecordName)
    private val eventType = ClassName(packageName, eventTypeName)
    private val cityItemType = ClassName("com.byagowi.persiancalendar.entities", cityItemName)

    private operator fun File.div(child: String) = File(this, child)

    @InputDirectory
    abstract fun getGeneratedAppSrcDir(): Property<File>

    @TaskAction
    fun action() {
        val generatedAppSrcDir = getGeneratedAppSrcDir().get()
        generatedAppSrcDir.mkdirs()
        val projectDir = project.projectDir
        val actions = listOf(
            "events" to ::generateEventsCode,
            "cities" to ::generateCitiesCode,
            "districts" to ::generateDistrictsCode
        )
        actions.forEach { (name, generator) ->
            val input = projectDir / "data" / "$name.json"
            val builder = FileSpec.builder(packageName, name.capitalized())
            generator(input, builder)
            builder.build().writeTo(generatedAppSrcDir)
        }
    }

    @Serializable
    data class EventStore(
        @SerialName("Source") val source: Map<String, String>,
        @SerialName("#meta") val meta: List<String>,
        @SerialName("Persian Calendar") val persianCalendar: List<Event>,
        @SerialName("Hijri Calendar") val islamicCalendar: List<Event>,
        @SerialName("Gregorian Calendar") val gregorianCalendar: List<Event>,
        @SerialName("Nepali Calendar") val nepaliCalendar: List<Event>,
        @SerialName("Irregular Recurring") val irregularRecurring: List<Map<String, JsonElement>>
    )

    @Serializable
    data class Event(
        val holiday: Boolean, val month: Int, val day: Int, val type: String, val title: String
    )

    private fun generateEventsCode(eventsJson: File, builder: FileSpec.Builder) {
        @OptIn(ExperimentalSerializationApi::class)
        val events = Json.decodeFromStream<EventStore>(eventsJson.inputStream())
        builder.addType(
            TypeSpec.enumBuilder(eventTypeName)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("source", String::class)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("source", String::class)
                        .initializer("source")
                        .build()
                )
                .also {
                    events.source.forEach { (name, source) ->
                        it.addEnumConstant(
                            name,
                            TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", source)
                                .build()
                        )
                    }
                }
                .build()
        )
        val calendarRecordFields = listOf(
            "title" to String::class.asClassName(),
            "type" to eventType,
            "isHoliday" to Boolean::class.asClassName(),
            "month" to Int::class.asClassName(),
            "day" to Int::class.asClassName()
        )
        builder.addType(
            TypeSpec.classBuilder(calendarRecordName)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .also {
                            calendarRecordFields.forEach { (name, type) ->
                                it.addParameter(name, type)
                            }
                        }
                        .build()
                )
                .also {
                    calendarRecordFields.forEach { (name, type) ->
                        it.addProperty(
                            PropertySpec.builder(name, type)
                                .initializer(name)
                                .build()
                        )
                    }
                }
                .build()
        )
        listOf(
            events.persianCalendar to "persianEvents",
            events.islamicCalendar to "islamicEvents",
            events.gregorianCalendar to "gregorianEvents",
            events.nepaliCalendar to "nepaliEvents"
        ).forEach { (list, field) ->
            builder.addProperty(
                PropertySpec
                    .builder(field, List::class.asClassName().parameterizedBy(calendarRecordType))
                    .initializer(buildCodeBlock {
                        addStatement("listOf(")
                        list.forEach {
                            withIndent {
                                addStatement("%L(", calendarRecordName)
                                withIndent {
                                    addStatement("title = %S,", it.title)
                                    add("type = EventType.%L, ", it.type)
                                    add("isHoliday = %L, ", it.holiday)
                                    add("month = %L, ", it.month)
                                    addStatement("day = %L", it.day)
                                }
                                addStatement("),")
                            }
                        }
                        add(")")
                    })
                    .build()
            )
        }
        @OptIn(ExperimentalStdlibApi::class)
        builder.addProperty(
            PropertySpec
                .builder("irregularRecurringEvents", typeNameOf<List<Map<String, String>>>())
                .initializer(
                    buildCodeBlock {
                        addStatement("listOf(")
                        events.irregularRecurring.forEach {
                            withIndent {
                                addStatement("mapOf(")
                                it.forEach { (k, v) ->
                                    withIndent {
                                        addStatement("%S to %S,", k, v.jsonPrimitive.content)
                                    }
                                }
                                addStatement("),")
                            }
                        }
                        add(")")
                    }
                )
                .build()
        )
    }

    @Serializable
    data class City(
        val en: String, val fa: String, val ckb: String, val ar: String,
        val latitude: Double, val longitude: Double, val elevation: Double
    )

    @Serializable
    data class Country(
        val en: String, val fa: String, val ckb: String, val ar: String,
        val cities: Map<String, City>
    )

    private fun generateCitiesCode(citiesJson: File, builder: FileSpec.Builder) {
        builder.addImport("com.byagowi.persiancalendar.entities", cityItemName)
        val coordinatesName = "Coordinates"
        builder.addImport("io.github.persiancalendar.praytimes", coordinatesName)
        @OptIn(ExperimentalSerializationApi::class)
        builder.addProperty(
            PropertySpec
                .builder(
                    "citiesStore",
                    Map::class.asClassName()
                        .parameterizedBy(String::class.asClassName(), cityItemType)
                )
                .initializer(buildCodeBlock {
                    addStatement("mapOf(")
                    Json.decodeFromStream<Map<String, Country>>(
                        citiesJson.inputStream()
                    ).forEach { countryEntry ->
                        val countryCode = countryEntry.key
                        val country = countryEntry.value
                        country.cities.forEach { cityEntry ->
                            val key = cityEntry.key
                            val city = cityEntry.value
                            val latitude = city.latitude
                            val longitude = city.longitude
                            // Elevation really degrades quality of calculations
                            val elevation = if (countryCode == "ir") .0 else city.elevation
                            withIndent {
                                addStatement("%S to %L(", key, cityItemName)
                                withIndent {
                                    addStatement("key = %S,", key)
                                    add("en = %S, ", city.en)
                                    add("fa = %S, ", city.fa)
                                    add("ckb = %S, ", city.ckb)
                                    addStatement("ar = %S,", city.ar)
                                    addStatement("countryCode = %S,", countryCode)
                                    add("countryEn = %S, ", country.en)
                                    add("countryFa = %S, ", country.fa)
                                    add("countryCkb = %S, ", country.ckb)
                                    addStatement("countryAr = %S,", country.ar)
                                    addStatement(
                                        "coordinates = %L(%L, %L, %L)",
                                        coordinatesName, latitude, longitude, elevation
                                    )
                                }
                                addStatement("),")
                            }
                        }
                    }
                    add(")")
                })
                .build()
        )
    }

    @Serializable
    data class Coordinates(
        @SerialName("lat") val latitude: Double,
        @SerialName("long") val longitude: Double
    )

    private fun generateDistrictsCode(districtsJson: File, builder: FileSpec.Builder) {
        @OptIn(ExperimentalStdlibApi::class, ExperimentalSerializationApi::class)
        builder.addProperty(
            PropertySpec.builder("districtsStore", typeNameOf<List<Pair<String, List<String>>>>())
                .initializer(buildCodeBlock {
                    addStatement("listOf(")
                    Json.decodeFromStream<Map<String, Map<String, Map<String, Coordinates>>>>(
                        districtsJson.inputStream()
                    ).forEach { province ->
                        val provinceName = province.key
                        withIndent {
                            addStatement("%S to listOf(", provinceName)
                            province.value.forEach { county ->
                                val key = county.key
                                withIndent {
                                    addStatement(
                                        "%S,",
                                        "$key;" + county.value.map { district ->
                                            val coordinates = district.value
                                            val latitude = coordinates.latitude
                                            val longitude = coordinates.longitude
                                            // Remove what is in the parenthesis
                                            val name = district.key.split("(")[0]
                                            "$name:$latitude:$longitude"
                                        }.joinToString(";")
                                    )
                                }
                            }
                            addStatement("),")
                        }
                    }
                    add(")")
                })
                .build()
        )
    }
}
