package io.github.persiancalendar.gradle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class CodeGenerators : DefaultTask() {

    private val packageName = "com.byagowi.persiancalendar.generated"

    private val calendarRecordName = "CalendarRecord"
    private val eventSourceName = "EventSource"
    private val cityItemName = "CityItem"

    private val calendarRecordType = ClassName(packageName, calendarRecordName)
    private val eventSource = ClassName(packageName, eventSourceName)
    private val cityItemType = ClassName("com.byagowi.persiancalendar.entities", cityItemName)

    private operator fun File.div(child: String) = File(this, child)

    @InputDirectory
    abstract fun getGeneratedAppSrcDir(): Property<File>

    @Input
    abstract fun getIsWear(): Property<Boolean>

    @get:Inject
    abstract val pl: ProjectLayout

    fun execute(target: Project, isWear: Boolean = false) {
        val projectDir = project.projectDir
        val generatedAppSrcDir =
            target.layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
        generatedAppSrcDir.mkdirs()
        setProperty("generatedAppSrcDir", generatedAppSrcDir)
        setProperty("isWear", isWear)
        val generateDir = generatedAppSrcDir / "com" / "byagowi" / "persiancalendar" / "generated"

        run {
            inputs.file(projectDir / "data" / "events" / "events.json")
            outputs.file(generateDir / "events.kt")
        }
        if (!isWear) listOf("cities", "districts").forEach { name ->
            val input = projectDir / "data" / "$name.json"
            inputs.file(input)
            val output = generateDir / "$name.kt"
            outputs.file(output)
        }

        if (isWear) {
            inputs.file(projectDir / "shaders" / "globe.agsl")
        } else {
            inputs.file(project.rootDir / "THANKS.md")
            inputs.file(project.rootDir / "FAQ.fa.md")
            inputs.file(projectDir / "shaders" / "common.vert")
            inputs.file(projectDir / "shaders" / "globe.frag")
            inputs.file(projectDir / "shaders" / "sandbox.frag")
        }
        outputs.file(generateDir / "TextStore.kt")
    }

    @TaskAction
    fun action() {
        val generatedAppSrcDir = getGeneratedAppSrcDir().get()
        generatedAppSrcDir.mkdirs()
        val projectDir = pl.projectDirectory.asFile
        val isWear = getIsWear().get()
        run {
            val input = projectDir / "data" / "events" / "events.json"
            val builder = FileSpec.builder(packageName, "events")
            generateEventsCode(input, builder)
            builder.build().writeTo(generatedAppSrcDir)
        }
        if (!isWear) listOf(
            "cities" to ::generateCitiesCode,
            "districts" to ::generateDistrictsCode,
        ).forEach { (name, generator) ->
            val input = projectDir / "data" / "$name.json"
            val builder = FileSpec.builder(packageName, name)
            generator(input, builder)
            builder.build().writeTo(generatedAppSrcDir)
        }
        createTextStore(generatedAppSrcDir, isWear)
    }

    private fun createTextStore(generatedAppSrcDir: File, isWear: Boolean) {
        val builder = FileSpec.builder(packageName, "TextStore")
        val projectDir = pl.projectDirectory.asFile
        val rootDir = projectDir.parentFile
        buildList {
            if (!isWear) add(rootDir / "THANKS.md" to "credits")
            if (!isWear) add(rootDir / "FAQ.fa.md" to "faq")
            if (!isWear) add(projectDir / "shaders" / "common.vert" to "commonVertexShader")
            if (!isWear) add(projectDir / "shaders" / "globe.frag" to "globeFragmentShader")
            if (isWear) add(projectDir / "shaders" / "globe.agsl" to "globeRuntimeShader")
            if (!isWear) add(projectDir / "shaders" / "sandbox.frag" to "sandboxFragmentShader")
        }.forEach { (textFile, fieldName) ->
            builder.addProperty(
                PropertySpec.builder(fieldName, String::class, KModifier.CONST)
                    .initializer(buildCodeBlock { addStatement("%S", textFile.readText()) })
                    .build(),
            )
        }
        builder.build().writeTo(generatedAppSrcDir)
    }

    @Serializable
    data class EventStore(
        @SerialName("Source") val source: Map<String, String>,
        @SerialName("#meta") val meta: List<String>,
        @SerialName("data") val data: List<Map<String, JsonElement>>,
    )

    data class Event(
        val holiday: Boolean, val month: Int, val day: Int, val type: String, val title: String,
        val calendar: String,
        val metadata: Map<String, JsonElement> = emptyMap(),
    )

    private fun generateEventsCode(eventsJson: File, builder: FileSpec.Builder) {
        @OptIn(ExperimentalSerializationApi::class)
        val events = Json.decodeFromStream<EventStore>(eventsJson.inputStream())
        builder.addType(
            TypeSpec.enumBuilder(eventSourceName)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("link", String::class)
                        .build(),
                )
                .addProperty(
                    PropertySpec.builder("link", String::class)
                        .initializer("link")
                        .build(),
                )
                .also {
                    events.source.forEach { (name, source) ->
                        it.addEnumConstant(
                            name,
                            TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", source)
                                .build(),
                        )
                    }
                }
                .build(),
        )
        val calendarRecordFields = listOf(
            "title" to typeNameOf<String>(),
            "source" to eventSource,
            "isHoliday" to typeNameOf<Boolean>(),
            "month" to typeNameOf<Int>(),
            "day" to typeNameOf<Int>(),
            "metadata" to typeNameOf<Map<String, Any>>(),
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
                        .build(),
                )
                .also {
                    calendarRecordFields.forEach { (name, type) ->
                        it.addProperty(
                            PropertySpec.builder(name, type)
                                .initializer(name)
                                .build(),
                        )
                    }
                }
                .build(),
        )
        events.data.forEach {
            val calendars = listOf("Persian", "Hijri", "Gregorian", "Nepali")
            assert(it["calendar"]?.jsonPrimitive?.content in calendars)
        }
        val simpleEvents = events.data.filter {
            it["rule"]?.jsonPrimitive?.content == "simple"
        }.map { element ->
            listOf("holiday", "month", "day", "type", "title", "calendar").forEach {
                assert(it in element.keys)
            }
            element.keys.all { key ->
                key in listOf("holiday", "month", "day", "type", "title", "calendar", "metadata")
            }
            Event(
                holiday = element["holiday"]?.jsonPrimitive?.boolean ?: false,
                month = element["month"]?.jsonPrimitive?.int ?: 0,
                day = element["day"]?.jsonPrimitive?.int ?: 0,
                type = element["type"]?.jsonPrimitive?.content.orEmpty(),
                title = element["title"]?.jsonPrimitive?.content.orEmpty(),
                calendar = element["calendar"]?.jsonPrimitive?.content.orEmpty(),
                metadata = element["metadata"]?.jsonObject?.toMap() ?: emptyMap(),
            )
        }
        listOf(
            simpleEvents.filter { it.calendar == "Persian" } to "persianEvents",
            simpleEvents.filter { it.calendar == "Hijri" } to "islamicEvents",
            simpleEvents.filter { it.calendar == "Gregorian" } to "gregorianEvents",
            simpleEvents.filter { it.calendar == "Nepali" } to "nepaliEvents",
        ).forEach { (list, field) ->
            builder.addProperty(
                PropertySpec
                    .builder(field, List::class.asClassName().parameterizedBy(calendarRecordType))
                    .initializer(
                        buildCodeBlock {
                            addStatement("listOf(")
                            list.forEach {
                                withIndent {
                                    addStatement("%L(", calendarRecordName)
                                    withIndent {
                                        addStatement("title = %S,", it.title)
                                        add("source = EventSource.%L, ", it.type)
                                        add("isHoliday = %L, ", it.holiday)
                                        add("month = %L, ", it.month)
                                        addStatement("day = %L, ", it.day)
                                        add("metadata = ")
                                        if (it.metadata.isEmpty()) add("emptyMap()") else {
                                            withIndent {
                                                addStatement("mapOf(")
                                                it.metadata.forEach { (k, v) ->
                                                    addStatement("%S to %L,", k, v)
                                                }
                                            }
                                            add(")")
                                        }
                                        addStatement(",")
                                    }
                                    addStatement("),")
                                }
                            }
                            add(")")
                        },
                    )
                    .build(),
            )
        }
        builder.addProperty(
            PropertySpec
                .builder("irregularRecurringEvents", typeNameOf<List<Map<String, Any>>>())
                .initializer(
                    buildCodeBlock {
                        addStatement("listOf(")
                        events.data.filter {
                            val rule = it["rule"]?.jsonPrimitive?.content
                            assert(rule != null)
                            rule != "simple"
                        }.forEach {
                            withIndent {
                                addStatement("mapOf(")
                                it.forEach { (k, v) ->
                                    withIndent { addStatement("%S to %L,", k, v) }
                                }
                                addStatement("),")
                            }
                        }
                        add(")")
                    },
                )
                .build(),
        )
    }

    @Serializable
    data class City(
        val en: String, val fa: String, val ckb: String, val ar: String,
        val latitude: Double, val longitude: Double, val elevation: Double,
    )

    @Serializable
    data class Country(
        val en: String, val fa: String, val ckb: String, val ar: String,
        val cities: Map<String, City>,
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
                        .parameterizedBy(String::class.asClassName(), cityItemType),
                )
                .initializer(
                    buildCodeBlock {
                        addStatement("mapOf(")
                        Json.decodeFromStream<Map<String, Country>>(
                            citiesJson.inputStream(),
                        ).forEach { countryEntry ->
                            val countryCode = countryEntry.key
                            val country = countryEntry.value
                            country.cities.forEach { cityEntry ->
                                val key = cityEntry.key
                                val city = cityEntry.value
                                val latitude = city.latitude
                                val longitude = city.longitude
                                // Elevation really degrades quality of calculations
                                val elevation = .0
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
                                            coordinatesName, latitude, longitude, elevation,
                                        )
                                    }
                                    addStatement("),")
                                }
                            }
                        }
                        add(")")
                    },
                )
                .build(),
        )
    }

    @Serializable
    data class Coordinates(
        @SerialName("lat") val latitude: Double,
        @SerialName("long") val longitude: Double,
    )

    private fun generateDistrictsCode(districtsJson: File, builder: FileSpec.Builder) {
        @OptIn(ExperimentalSerializationApi::class)
        builder.addProperty(
            PropertySpec.builder("districtsStore", typeNameOf<Map<String, List<String>>>())
                .initializer(
                    buildCodeBlock {
                        addStatement("mapOf(")
                        Json.decodeFromStream<Map<String, Map<String, Map<String, Coordinates>>>>(
                            districtsJson.inputStream(),
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
                                            }.joinToString(";"),
                                        )
                                    }
                                }
                                addStatement("),")
                            }
                        }
                        add(")")
                    },
                )
                .build(),
        )
    }
}
