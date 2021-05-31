import java.net.URI

sourceControl {
    listOf("equinox", "calendar", "praytimes").forEach {
        gitRepository(URI.create("https://github.com/persian-calendar/$it.git")) {
            producesModule("io.github.persiancalendar:$it")
        }
    }
}

rootProject.buildFileName = "build.gradle.kts"
include(":PersianCalendar")
