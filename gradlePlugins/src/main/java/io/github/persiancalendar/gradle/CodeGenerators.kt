package io.github.persiancalendar.gradle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.typeNameOf
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

class CodeGenerators : Plugin<Project> {

    private val packageName = "com.byagowi.persiancalendar.generated"
    private operator fun File.div(child: String) = File(this, child)

    override fun apply(target: Project) {
        target.tasks.register("codegenerators") {
            val projectDir = target.projectDir
            val generatedAppSrcDir = target.buildDir / "generated" / "source" / "appsrc" / "main"
            val generateDir =
                generatedAppSrcDir / "com" / "byagowi" / "persiancalendar" / "generated"

            val actions = listOf(
                "events" to ::generateEventsCode,
                "cities" to ::generateCitiesCode,
                "districts" to ::generateDistrictsCode
            )
            actions.forEach { (name, _) ->
                val input = projectDir / "data" / "$name.json"
                inputs.file(input)
                val output = generateDir / "${name.capitalized()}.kt"
                outputs.file(output)
            }

            doLast {
                actions.forEach { (name, generator) ->
                    val input = projectDir / "data" / "$name.json"
                    val builder = FileSpec.builder(packageName, name.capitalized())
                    generator(input, builder)
                    builder.build().writeTo(generatedAppSrcDir)
                }
            }
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
                "CalendarRecord(title = \"${record["title"]}\"," +
                        " type = EventType.${record["type"]}," +
                        " isHoliday = ${record["holiday"]}," +
                        " month = ${record["month"]}, day = ${record["day"]})"
            }
        }
        val irregularRecurringEvents = (events["Irregular Recurring"] as List<*>)
            .mapNotNull { it as Map<*, *> }
            .joinToString(",\n") { event ->
                "mapOf(${event.map { (k, v) -> """"$k" to "$v"""" }.joinToString(", ")})"
            }
        builder.addType(
            TypeSpec.enumBuilder("EventType")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("source", String::class)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("source", String::class, KModifier.PUBLIC)
                        .initializer("source")
                        .build()
                )
                .also {
                    (events["Source"] as Map<*, *>).toList().forEach { (name, source) ->
                        it.addEnumConstant(
                            name as String, TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", source as String)
                                .build()
                        )
                    }
                }
                .build()
        )
        builder.addType(
            TypeSpec.classBuilder("CalendarRecord")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("title", String::class)
                        .addParameter("type", ClassName(packageName, "EventType"))
                        .addParameter("isHoliday", Boolean::class)
                        .addParameter("month", Int::class)
                        .addParameter("day", Int::class)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("title", String::class, KModifier.PUBLIC)
                        .initializer("title")
                        .build()
                )
                .addProperty(
                    PropertySpec
                        .builder("type", ClassName(packageName, "EventType"), KModifier.PUBLIC)
                        .initializer("type")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("isHoliday", Boolean::class, KModifier.PUBLIC)
                        .initializer("isHoliday")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("month", Int::class, KModifier.PUBLIC)
                        .initializer("month")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("day", Int::class, KModifier.PUBLIC)
                        .initializer("day")
                        .build()
                )
                .build()
        )
        val calendarRecordList = List::class.asClassName()
            .parameterizedBy(ClassName(packageName, "CalendarRecord"))
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
                    CodeBlock.of("listOf(\n$irregularRecurringEvents\n)".preventLineWraps())
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
