package io.github.persiancalendar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.zip.GZIPInputStream

private const val ICON_BASE_URL = "https://fonts.gstatic.com/render/v1"
private const val ICON_PACKAGE_PREFIX = "com.byagowi.persiancalendar.ui.icons.material"

private val AUTO_MIRRORED_ICONS = setOf(
    "arrow_back",
    "backspace",
    "help",
    "keyboard_arrow_left",
    "keyboard_arrow_right",
    "open_in_new",
)

abstract class MaterialSymbolsDownload : DefaultTask() {

    @get:Input
    abstract val iconsByVariant: MapProperty<String, List<String>>

    @get:OutputDirectory
    abstract val outputRootDir: DirectoryProperty

    @TaskAction
    fun run() {
        val root = outputRootDir.get().asFile
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
        iconsByVariant.get().forEach { (variant, icons) ->
            val variantDir = File(root, variant)
            variantDir.mkdirs()
            val (family, fill) = when (variant) {
                "filled" -> "Outlined" to 1
                "outlined" -> "Outlined" to 0
                "rounded" -> "Rounded" to 1
                "sharp" -> "Sharp" to 1
                else -> variant.replaceFirstChar { it.uppercaseChar() } to 1
            }
            val packageName = "$ICON_PACKAGE_PREFIX.$variant"
            icons.forEach { fileName ->
                val url = "$ICON_BASE_URL/Material+Symbols+$family/24dp/$fileName.kt?var=opsz,wght,FILL,GRAD,ROND@24,400,$fill,0,50"
                val content = download(client, url) ?: return@forEach
                val target = File(variantDir, "$fileName.kt")
                target.writeText(
                    content.replacePackageAndName(packageName, variant).let {
                        if (fileName in AUTO_MIRRORED_ICONS) it.withAutoMirror() else it
                    },
                )
                logger.lifecycle("Downloaded $url -> ${target.relativeTo(project.projectDir)}")
            }
        }
    }

    private fun download(client: HttpClient, url: String): String? {
        val request = HttpRequest.newBuilder(URI.create(url)).header("Accept-Encoding", "gzip")
            .timeout(Duration.ofSeconds(60)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != 200) {
            logger.warn("Skipping $url: HTTP ${response.statusCode()}")
            return null
        }
        val encoding = response.headers().firstValue("Content-Encoding").orElse("")
        val stream = if (encoding.contains("gzip", ignoreCase = true)) {
            GZIPInputStream(response.body())
        } else {
            response.body()
        }
        return stream.bufferedReader().use { it.readText() }
    }
}

private fun String.replacePackageAndName(pkg: String, variant: String): String =
    this.replaceFirst("package com.example.test", "package $pkg")
        .replace(Regex("public val ([\\da-z_]+): ImageVector")) { matchResult ->
            val newName =
                matchResult.groupValues[1].let { if (variant == "filled") it else "${it}_$variant" }
                    .replace(Regex("_[a-z]")) { it.value.drop(1).uppercase() }
                    .replace(Regex("_\\d")) { it.value.drop(1) }
            "public val ${newName}Icon: ImageVector"
        }.replaceFirst(Regex("public val (\\d)"), "public val _$1")

private fun String.withAutoMirror(): String =
    replaceFirst("ImageVector.Builder(", "ImageVector.Builder(autoMirror = true, ")
