package io.github.persiancalendar.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal fun generatedAppSourceDir(project: Project): Provider<Directory> =
    project.layout.buildDirectory.dir("generated/source/appsrc/main")

class AppBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Codegen runs only in the module(s) that bundle the source data
        // (currently the shared KMP module).
        if (target.file("data/events/events.json").exists()) {
            val taskProvider = target.tasks.register("codegenerators", CodeGenerators::class.java) {
                configure()
            }

            target.plugins.withId("com.android.application") {
                val androidComponents =
                    target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
                androidComponents.onVariants { variant ->
                    variant.sources.kotlin?.addGeneratedSourceDirectory(
                        taskProvider,
                        CodeGenerators::getGeneratedAppSrcDir,
                    )
                }
            }
        }

        // ./gradlew :shared:downloadIcons
        if (target.name == "shared") target.tasks.register(
            "downloadIcons",
            MaterialSymbolsDownload::class.java,
        ) {
            iconsByVariant.set(
                mapOf(
                    "filled" to listOf(
                        "3d_rotation", "add", "android", "arrow_back", "backspace", "brightness_4",
                        "brightness_7", "calendar_month", "calendar_view_day", "calendar_view_week",
                        "cancel", "check", "close", "construction", "date_range", "delete", "done",
                        "edit", "email", "expand_more", "explore", "folder", "fullscreen",
                        "fullscreen_exit", "grid_3x3", "help", "image", "info",
                        "keyboard_arrow_down", "keyboard_arrow_left", "keyboard_arrow_right",
                        "keyboard_arrow_up", "location_on", "map", "menu", "mode_night",
                        "more_horiz", "more_vert", "motorcycle", "my_location", "nightlight_round",
                        "open_in_browser", "open_in_new", "palette", "perm_device_information",
                        "play_arrow", "print", "remove_circle_outline", "restore", "search",
                        "settings", "settings_backup_restore", "share", "social_distance",
                        "sports_esports", "stop", "swap_vertical_circle", "swipe_down", "swipe_up",
                        "sync_alt", "translate", "widgets", "yard",
                    ),
                    "outlined" to listOf("light_mode", "location_on", "palette", "widgets"),
                    "rounded" to listOf("drag_handle"),
                ),
            )
            outputRootDir.set(
                target.layout.projectDirectory.dir("src/commonMain/kotlin/com/byagowi/persiancalendar/icons/material"),
            )
            outputs.upToDateWhen { false }
        }

        target.tasks.register("updateDependenciesReport", DependenciesReport::class.java) {
            configurationName.set("releaseRuntimeClasspath")
            reportFile.set(target.layout.projectDirectory.file("runtime-dependencies-report.txt"))
        }

        // guard against duplicate task registration (wear, the main app)
        val root = target.rootProject
        val checkSubmodules = root.tasks.findByName("checkSubmodules") ?: run {
            root.tasks.register("checkSubmodules", SubmoduleCheck::class.java)
        }
        target.tasks.matching {
            it.name == "preReleaseBuild" || it.name == "preNightlyBuild"
        }.configureEach { dependsOn(checkSubmodules) }
    }
}
