plugins {
    // All the plugins used in subprojects and plugins should be listed here with "apply false"

    // PersianCalendar plugins
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("io.github.persiancalendar.appbuildplugin") apply false

    // gradlePlugins plugins
    `kotlin-dsl` apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.parcelize) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.spotless)
}

spotless {
    val editorConfigOverride = mapOf(
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_wrapping" to "disabled",
        "ktlint_standard_argument-list-wrapping" to "disabled",
        "ktlint_standard_parameter-list-wrapping" to "disabled",
        "ktlint_standard_comment-wrapping" to "disabled",
        "ktlint_standard_function-signature" to "disabled",
        "ktlint_standard_statement-wrapping" to "disabled",
        "ktlint_standard_value-parameter-comment" to "disabled",
        "ktlint_standard_max-line-length" to "disabled",
        "ktlint_standard_indent" to "disabled",
        "ktlint_standard_annotation" to "disabled",
        "ktlint_standard_function-naming" to "disabled",
        "ktlint_standard_property-naming" to "disabled",
        "ktlint_standard_function-expression-body" to "disabled",
        "ktlint_standard_multiline-if-else" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
        "ktlint_standard_blank-line-between-when-conditions" to "disabled",
        "ktlint_standard_kdoc" to "disabled",
        "ktlint_standard_enum-wrapping" to "disabled",
    )

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(editorConfigOverride)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(editorConfigOverride)
    }

    format("misc") {
        target("**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register("updatePersianCalendarVersions") {
    group = "versioning"
    description = "Replaces the persian-calendar dependency versions in the version catalog with the latest commit on the main branch."

    val versionCatalog = rootProject.file("gradle/libs.versions.toml")
    val persianCalendarRepositories = mapOf(
        "persiancalendar-calculator" to "calculator",
        "persiancalendar-calendar" to "calendar",
        "persiancalendar-equinox" to "equinox",
        "persiancalendar-praytimes" to "praytimes",
        "persiancalendar-qr" to "qr",
    )

    doLast {
        val client = java.net.http.HttpClient.newHttpClient()
        val token = System.getenv("GITHUB_TOKEN")
        var content = versionCatalog.readText()

        for ((key, repo) in persianCalendarRepositories) {
            val requestBuilder = java.net.http.HttpRequest.newBuilder(
                java.net.URI("https://api.github.com/repos/persian-calendar/$repo/commits/main"),
            )
                .header("Accept", "application/vnd.github+json")
                .GET()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val response = client.send(
                requestBuilder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofString(),
            )
            require(response.statusCode() == 200) {
                "Failed to fetch latest commit for $repo (HTTP ${response.statusCode()}): ${response.body()}"
            }

            val sha = Regex("\"sha\"\\s*:\\s*\"([0-9a-f]{40})\"")
                .find(response.body())
                ?.groupValues
                ?.get(1)
                ?: error("Could not parse commit SHA for $repo")

            val pattern = Regex("($key\\s*=\\s*)\"[0-9a-f]+\"")
            content = pattern.replace(content) { match -> "${match.groupValues[1]}\"$sha\"" }

            logger.lifecycle("$repo -> $sha")
        }

        versionCatalog.writeText(content)
        logger.lifecycle("Updated ${versionCatalog.name}")
    }
}
