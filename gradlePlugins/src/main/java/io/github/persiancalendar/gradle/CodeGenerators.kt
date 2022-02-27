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
import com.squareup.kotlinpoet.withIndent
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
    private val cityItemName = "CityItem"

    private val calendarRecordType = ClassName(packageName, calendarRecordName)
    private val eventType = ClassName(packageName, eventTypeName)
    private val cityItemType = ClassName("com.byagowi.persiancalendar.entities", cityItemName)

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
        listOf(
            "Persian Calendar" to "persianEvents",
            "Hijri Calendar" to "islamicEvents",
            "Gregorian Calendar" to "gregorianEvents",
            "Nepali Calendar" to "nepaliEvents"
        ).forEach { (key, field) ->
            builder.addProperty(
                PropertySpec
                    .builder(field, List::class.asClassName().parameterizedBy(calendarRecordType))
                    .initializer(buildCodeBlock {
                        add("listOf(\n")
                        (events[key] as List<*>).forEach {
                            val record = it as Map<*, *>
                            add(
                                """%L(
                                |    title = %S,
                                |    type = EventType.%L, isHoliday = %L, month = %L, day = %L
                                |),
                                |""".trimMargin().preventLineWraps(),
                                calendarRecordName, record["title"], record["type"],
                                record["holiday"], record["month"], record["day"]
                            )
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
        builder.addImport("com.byagowi.persiancalendar.entities", cityItemName)
        builder.addImport("io.github.persiancalendar.praytimes", "Coordinates")
        builder.addProperty(
            PropertySpec
                .builder(
                    "citiesStore",
                    Map::class.asClassName()
                        .parameterizedBy(String::class.asClassName(), cityItemType)
                )
                .initializer(buildCodeBlock {
                    add("mapOf(\n")
                    (JsonSlurper().parse(citiesJson) as Map<*, *>).forEach { countryEntry ->
                        val countryCode = countryEntry.key as String
                        val country = countryEntry.value as Map<*, *>
                        (country["cities"] as Map<*, *>).forEach { cityEntry ->
                            val key = cityEntry.key as String
                            val city = cityEntry.value as Map<*, *>
                            val latitude = (city["latitude"] as Number).toDouble()
                            val longitude = (city["longitude"] as Number).toDouble()
                            // Elevation really degrades quality of calculations
                            val elevation =
                                if (countryCode == "ir") .0 else (city["elevation"] as Number).toDouble()
                            withIndent {
                                addStatement("%S to %L(", key, cityItemName)
                                withIndent {
                                    addStatement("key = %S,", key)
                                    add("en = %S, ", city["en"])
                                    addStatement("fa = %S,", city["fa"])
                                    add("ckb = %S, ", city["ckb"])
                                    addStatement("ar = %S,", city["ar"])
                                    addStatement("countryCode = %S,", countryCode)
                                    add("countryEn = %S, ", country["en"])
                                    addStatement("countryFa = %S,", country["fa"])
                                    add("countryCkb = %S, ", country["ckb"])
                                    addStatement("countryAr = %S,", country["ar"])
                                    addStatement(
                                        "coordinates = %L",
                                        "Coordinates($latitude, $longitude, $elevation)"
                                    )
                                }
                                add("),\n")
                            }
                        }
                    }
                    add(")")
                })
                .build()
        )
    }

    private fun generateDistrictsCode(districtsJson: File, builder: FileSpec.Builder) {
        @OptIn(ExperimentalStdlibApi::class)
        builder.addProperty(
            PropertySpec.builder("districtsStore", typeNameOf<List<Pair<String, List<String>>>>())
                .initializer(buildCodeBlock {
                    add("listOf(\n")
                    (JsonSlurper().parse(districtsJson) as Map<*, *>).forEach { province ->
                        val provinceName = province.key as String
                        if (provinceName.startsWith("#")) return@forEach
                        add("%S to listOf(\n", provinceName)
                        (province.value as Map<*, *>).forEach { county ->
                            val key = county.key as String
                            add(
                                "    %S,\n".preventLineWraps(),
                                "$key;" + (county.value as Map<*, *>).map { district ->
                                    val coordinates = district.value as Map<*, *>
                                    val latitude = (coordinates["lat"] as Number).toDouble()
                                    val longitude = (coordinates["long"] as Number).toDouble()
                                    // Remove what is in the parenthesis
                                    val name = district.key.toString().split("(")[0]
                                    "$name:$latitude:$longitude"
                                }.joinToString(";")
                            )
                        }
                        add("),\n")
                    }
                    add("\n)")
                })
                .build()
        )
    }
}
