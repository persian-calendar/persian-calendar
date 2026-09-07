import groovy.json.JsonOutput
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.byagowi.persiancalendar.shared"
        compileSdk = 37
        minSdk = 23
        withHostTestBuilder {}
    }
    jvm()
    wasmJs {
        // Compose's browser-test runner needs an executable to bundle Skiko.
        binaries.executable()
        browser {
            testTask { useKarma { useChromeHeadless() } }
        }
    }
    jvmToolchain(21)
    sourceSets {
        androidMain { kotlin.srcDir("src/jvmMain/kotlin") }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

val sharedResources = layout.buildDirectory.dir("generated/sharedResources")
val generateSharedResources by tasks.registering {
    val androidResources = rootProject.file("PersianCalendar/src/main/res")
    val events = rootProject.file("PersianCalendar/data/events/events.json")
    inputs.dir(androidResources)
    inputs.file(events)
    inputs.file(rootProject.file("PersianCalendar/data/cities.json"))
    inputs.file(rootProject.file("FAQ.fa.md"))
    inputs.dir("licenses")
    outputs.dir(sharedResources)
    doLast {
        check(events.isFile) { "Initialize bundled events: git submodule update --init PersianCalendar/data/events" }
        val files = sharedResources.get().dir("files").asFile.apply { mkdirs() }
        val translations = linkedMapOf<String, Map<String, String>>()
        androidResources.listFiles().orEmpty().filter { it.name == "values" || it.name.startsWith("values-") }
            .sortedBy { it.name }.forEach { directory ->
                val strings = linkedMapOf<String, String>()
                directory.listFiles().orEmpty().filter { it.extension == "xml" }.forEach { xml ->
                    val factory = DocumentBuilderFactory.newInstance()
                    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                    val nodes = factory.newDocumentBuilder().parse(xml).getElementsByTagName("string")
                    repeat(nodes.length) { index ->
                        val node = nodes.item(index)
                        strings[node.attributes.getNamedItem("name").nodeValue] = node.textContent
                            .removeSurrounding("\"").replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n")
                    }
                }
                strings["code"]?.let { translations[it] = strings }
            }
        files.resolve("strings.json").writeText(JsonOutput.toJson(translations))
        events.copyTo(files.resolve("events.json"), overwrite = true)
        rootProject.file("PersianCalendar/data/cities.json").copyTo(files.resolve("cities.json"), overwrite = true)
        rootProject.file("FAQ.fa.md").copyTo(files.resolve("help.txt"), overwrite = true)
        rootProject.file("COPYING").copyTo(files.resolve("license.txt"), overwrite = true)
        file("licenses").listFiles().orEmpty().forEach { it.copyTo(files.resolve(it.name), overwrite = true) }
        listOf("worldmap", "timezones", "tectonicplates").forEach {
            androidResources.resolve("raw/$it.txt").copyTo(files.resolve("$it.txt"), overwrite = true)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.byagowi.persiancalendar.shared.generated"
    customDirectory(sourceSetName = "commonMain", directoryProvider = sharedResources)
}
tasks.matching { it.name in listOf("copyNonXmlValueResourcesForCommonMain", "convertXmlValueResourcesForCommonMain") }.configureEach {
    dependsOn(generateSharedResources)
}
