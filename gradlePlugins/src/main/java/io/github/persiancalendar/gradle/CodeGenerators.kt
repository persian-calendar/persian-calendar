package io.github.persiancalendar.gradle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.typeNameOf
import groovy.json.JsonSlurper
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

    private val calendarRecordType = ClassName(packageName, calendarRecordName)
    private val eventType = ClassName(packageName, eventTypeName)

    private operator fun File.div(child: String) = File(this, child)

    @InputDirectory
    abstract fun getGeneratedAppSrcDir(): Property<File>

    @TaskAction
    fun action() {
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
            builder.build().writeTo(getGeneratedAppSrcDir().get())
        }
    }

    // "·" prevents line wraps in KotlinPoet
    // https://square.github.io/kotlinpoet/#spaces-wrap-by-default
    private fun String.preventLineWraps(): String = this.replace(" ", "·")

    private fun generateEventsCode(eventsJson: File, builder: FileSpec.Builder) {
        val events = JsonSlurper().parse(eventsJson) as Map<*, *>
        val (persianEvents, islamicEvents, gregorianEvents, nepaliEvents) = listOf(
            "Persian Calendar", "Hijri Calendar", "Gregorian Calendar", "Nepali Calendar"
        ).map { key ->
            (events[key] as List<*>).joinToString(",\n") {
                val record = it as Map<*, *>
                buildCodeBlock {
                    add("%L(".preventLineWraps(), calendarRecordName)
                    add("title = %S, ".preventLineWraps(), record["title"])
                    add("type = EventType.%L, ".preventLineWraps(), record["type"])
                    add("isHoliday = %L, ".preventLineWraps(), record["holiday"])
                    add("month = %L, ".preventLineWraps(), record["month"])
                    add("day = %L".preventLineWraps(), record["day"])
                    add(")")
                }.toString()
            }
        }
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
                    (events["Source"] as Map<*, *>).forEach { (name, source) ->
                        it.addEnumConstant(
                            name as String, TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", source as String)
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
        val calendarRecordList = List::class.asClassName()
            .parameterizedBy(calendarRecordType)
        builder.addProperty(
            PropertySpec
                .builder("persianEvents", calendarRecordList)
                .initializer(CodeBlock.of("listOf(\n$persianEvents\n)".preventLineWraps()))
                .build()
        )
        builder.addProperty(
            PropertySpec
                .builder("islamicEvents", calendarRecordList)
                .initializer(CodeBlock.of("listOf(\n$islamicEvents\n)".preventLineWraps()))
                .build()
        )
        builder.addProperty(
            PropertySpec
                .builder("gregorianEvents", calendarRecordList)
                .initializer(CodeBlock.of("listOf(\n$gregorianEvents\n)".preventLineWraps()))
                .build()
        )
        builder.addProperty(
            PropertySpec
                .builder("nepaliEvents", calendarRecordList)
                .initializer(CodeBlock.of("listOf(\n$nepaliEvents\n)".preventLineWraps()))
                .build()
        )
        @OptIn(ExperimentalStdlibApi::class)
        builder.addProperty(
            PropertySpec
                .builder("irregularRecurringEvents", typeNameOf<List<Map<String, String>>>())
                .initializer(
                    buildCodeBlock {
                        add("listOf(\n")
                        (events["Irregular Recurring"] as List<*>).forEach {
                            add("mapOf(")
                            (it as Map<*, *>).forEach { (k, v) ->
                                add("%S to %S, ".preventLineWraps(), k, v)
                            }
                            add("),\n")
                        }
                        add(")")
                    }
                )
                .build()
        )
    }

    private fun generateCitiesCode(citiesJson: File, builder: FileSpec.Builder) {
        val cities = (JsonSlurper().parse(citiesJson) as Map<*, *>).flatMap { countryEntry ->
            val countryCode = countryEntry.key as String
            val country = countryEntry.value as Map<*, *>
            (country["cities"] as Map<*, *>).map { cityEntry ->
                val key = cityEntry.key as String
                val city = cityEntry.value as Map<*, *>
                val latitude = (city["latitude"] as Number).toDouble()
                val longitude = (city["longitude"] as Number).toDouble()
                // Elevation really degrades quality of calculations
                val elevation =
                    if (countryCode == "ir") .0 else (city["elevation"] as Number).toDouble()
                """"$key" to CityItem(
    key = "$key",
    en = "${city["en"]}", fa = "${city["fa"]}",
    ckb = "${city["ckb"]}", ar = "${city["ar"]}",
    countryCode = "$countryCode",
    countryEn = "${country["en"]}", countryFa = "${country["fa"]}",
    countryCkb = "${country["ckb"]}", countryAr = "${country["ar"]}",
    coordinates = Coordinates($latitude, $longitude, $elevation)
)"""
            }
        }.joinToString(",\n")
        val cityItem = ClassName("com.byagowi.persiancalendar.entities", "CityItem")
        builder.addImport("com.byagowi.persiancalendar.entities", "CityItem")
        builder.addImport("io.github.persiancalendar.praytimes", "Coordinates")
        builder.addProperty(
            PropertySpec
                .builder(
                    "citiesStore",
                    Map::class.asClassName().parameterizedBy(String::class.asClassName(), cityItem)
                )
                .initializer(CodeBlock.of("mapOf(\n$cities\n)".preventLineWraps()))
                .build()
        )
    }

    private fun generateDistrictsCode(districtsJson: File, builder: FileSpec.Builder) {
        val districts = (JsonSlurper().parse(districtsJson) as Map<*, *>).mapNotNull { province ->
            val provinceName = province.key as String
            if (provinceName.startsWith("#")) return@mapNotNull null
            "\"$provinceName\" to listOf(\n" + (province.value as Map<*, *>).map { county ->
                val key = county.key as String
                """    "$key;""" + (county.value as Map<*, *>).map { district ->
                    val coordinates = district.value as Map<*, *>
                    val latitude = (coordinates["lat"] as Number).toDouble()
                    val longitude = (coordinates["long"] as Number).toDouble()
                    // Remove what is in the parenthesis
                    val name = district.key.toString().split("(")[0]
                    "$name:$latitude:$longitude"
                }.joinToString(";") + "\""
            }.joinToString(",\n") + "\n)"
        }.joinToString(",\n")
        @OptIn(ExperimentalStdlibApi::class)
        builder.addProperty(
            PropertySpec.builder("districtsStore", typeNameOf<List<Pair<String, List<String>>>>())
                .initializer(CodeBlock.of("listOf(\n$districts\n)".preventLineWraps()))
                .build()
        )
    }
}
