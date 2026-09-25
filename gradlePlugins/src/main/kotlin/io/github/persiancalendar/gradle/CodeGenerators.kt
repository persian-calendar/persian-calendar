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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

abstract class CodeGenerators : DefaultTask() {

    private val packageName = "com.byagowi.persiancalendar.generated"

    private val calendarRecordName = "CalendarRecord"
    private val eventSourceName = "EventSource"
    private val cityItemName = "CityItem"

    private val calendarRecordType = ClassName(packageName, calendarRecordName)
    private val eventSource = ClassName(packageName, eventSourceName)
    private val cityItemType = ClassName("com.byagowi.persiancalendar.entities", cityItemName)

    @OutputDirectory
    abstract fun getGeneratedAppSrcDir(): DirectoryProperty

    @OutputDirectory
    abstract fun getGeneratedAppSrcDirAndroidMain(): DirectoryProperty

    @get:Inject
    abstract val projectLayout: ProjectLayout

    fun configure() {
        getGeneratedAppSrcDir().set(generatedAppSourceDir(project))
        getGeneratedAppSrcDirAndroidMain().set(project.layout.buildDirectory.dir("generated/source/appsrc/androidMain"))
        val projectDir = projectLayout.projectDirectory.asFile
        val rootDir = projectDir.parentFile

        inputs.file(projectDir.resolve("data/events/events.json"))
        listOf("cities", "districts").forEach { name ->
            inputs.file(projectDir.resolve("data/$name.json"))
        }
        inputs.file(projectDir.resolve("shaders/globe.agsl"))
        inputs.file(rootDir.resolve("THANKS.md"))
        inputs.file(rootDir.resolve("FAQ.fa.md"))
        inputs.file(projectDir.resolve("shaders/common.vert"))
        inputs.file(projectDir.resolve("shaders/globe.frag"))
        inputs.file(projectDir.resolve("shaders/sandbox.frag"))
        inputs.file(projectDir.resolve("data/worldmap.txt"))
        inputs.file(projectDir.resolve("data/timezones.txt"))
        inputs.file(projectDir.resolve("data/tectonicplates.txt"))
        inputs.files(
            project.fileTree(projectDir.resolve("src/androidMain/res")).matching {
                include("**/*.xml")
            },
        )
    }

    @TaskAction
    fun action() {
        val generatedDir = getGeneratedAppSrcDir().get().asFile
        generatedDir.mkdirs()
        val projectDir = projectLayout.projectDirectory.asFile
        run {
            val input = projectDir.resolve("data/events/events.json")
            val builder = FileSpec.builder(packageName, "events")
            generateEventsCode(input, builder)
            builder.build().writeTo(generatedDir)
        }
        listOf(
            "cities" to ::generateCitiesCode,
            "districts" to ::generateDistrictsCode,
        ).forEach { (name, generator) ->
            val input = projectDir.resolve("data/$name.json")
            val builder = FileSpec.builder(packageName, name)
            generator(input, builder)
            builder.build().writeTo(generatedDir)
        }
        createTextStore(generatedDir)
        createAndroidStringIds(
            resDir = projectDir.resolve("src/androidMain/res"),
            outputDir = getGeneratedAppSrcDirAndroidMain().get().asFile,
        )
    }

    private fun createTextStore(generatedAppSrcDir: File) {
        val builder = FileSpec.builder(packageName, "TextStore")
        val projectDir = projectLayout.projectDirectory.asFile
        val rootDir = projectDir.parentFile
        buildList {
            add(rootDir.resolve("THANKS.md") to "credits")
            add(rootDir.resolve("FAQ.fa.md") to "faq")
            add(projectDir.resolve("shaders/common.vert") to "commonVertexShader")
            add(projectDir.resolve("shaders/globe.frag") to "globeFragmentShader")
            add(projectDir.resolve("shaders/globe.agsl") to "globeRuntimeShader")
            add(projectDir.resolve("shaders/sandbox.frag") to "sandboxFragmentShader")
        }.forEach { (textFile, fieldName) ->
            builder.addProperty(
                PropertySpec.builder(fieldName, String::class, KModifier.CONST)
                    .initializer(buildCodeBlock { addStatement("%S", textFile.readText()) })
                    .build(),
            )
        }
        listOf(
            "worldmap" to "data/worldmap.txt",
            "timezones" to "data/timezones.txt",
            "tectonicplates" to "data/tectonicplates.txt",
        ).forEach { (fieldName, path) ->
            builder.addProperty(
                PropertySpec.builder(fieldName, String::class)
                    .initializer(buildCodeBlock { addStatement("%S", projectDir.resolve(path).readText()) })
                    .build(),
            )
        }
        builder.build().writeTo(generatedAppSrcDir)
    }

    private val kotlinHardKeywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "interface", "is", "null", "object", "package", "return", "super",
        "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    )

    private fun createAndroidStringIds(resDir: File, outputDir: File) {
        val builder = FileSpec.builder(packageName, "AndroidStringIds")
        val rClass = ClassName("com.byagowi.persiancalendar.shared", "R")
        val stringResourceClass = ClassName("org.jetbrains.compose.resources", "StringResource")
        val pluralStringResourceClass = ClassName("org.jetbrains.compose.resources", "PluralStringResource")
        builder.addImport("com.byagowi.persiancalendar.utils", "debugAssertNotNull")

        builder.addProperty(
            PropertySpec.builder("stringIds", typeNameOf<Map<String, Int>>(), KModifier.PRIVATE)
                .initializer(
                    buildCodeBlock {
                        addStatement("mapOf(")
                        collectResourceNames(resDir, "string").forEach { name ->
                            val field = if (name in kotlinHardKeywords) "`$name`" else name
                            withIndent { addStatement("%S to %T.string.%L,", name, rClass, field) }
                        }
                        add(")")
                    },
                )
                .build(),
        )
        builder.addProperty(
            PropertySpec.builder("pluralIds", typeNameOf<Map<String, Int>>(), KModifier.PRIVATE)
                .initializer(
                    buildCodeBlock {
                        addStatement("mapOf(")
                        collectResourceNames(resDir, "plurals").forEach { name ->
                            val field = if (name in kotlinHardKeywords) "`$name`" else name
                            withIndent { addStatement("%S to %T.plurals.%L,", name, rClass, field) }
                        }
                        add(")")
                    },
                )
                .build(),
        )
        builder.addProperty(
            PropertySpec.builder("stringId", typeNameOf<Int>())
                .receiver(stringResourceClass)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return stringIds[this.key].debugAssertNotNull ?: %T.string.empty", rClass)
                        .build(),
                )
                .build(),
        )
        builder.addProperty(
            PropertySpec.builder("pluralId", typeNameOf<Int>())
                .receiver(pluralStringResourceClass)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return pluralIds[this.key].debugAssertNotNull ?: %T.plurals.empty", rClass)
                        .build(),
                )
                .build(),
        )
        outputDir.mkdirs()
        builder.build().writeTo(outputDir)
    }

    private fun collectResourceNames(resDir: File, tagName: String): Set<String> {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        val names = sortedSetOf<String>()
        resDir.listFiles().orEmpty()
            .filter { it.isDirectory && (it.name == "values" || it.name.startsWith("values-")) }
            .forEach { dir ->
                dir.listFiles().orEmpty()
                    .filter { it.extension == "xml" }
                    .forEach { xml ->
                        val document = factory.newDocumentBuilder().parse(xml)
                        val nodes = document.getElementsByTagName(tagName)
                        repeat(nodes.length) { index ->
                            val name = nodes.item(index).attributes.getNamedItem("name")?.nodeValue
                            if (name != null) names.add(name)
                        }
                    }
            }
        return names
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
