package io.github.persiancalendar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Renders the resolved dependency tree of a configuration to a file using the
 * Gradle resolution API (no shell, no parsing of the `dependencies` task output).
 *
 * The format matches Gradle's own ASCII dependency report closely:
 * `+---`/`\---` connectors, `requested -> selected` version conflicts, `(c)` for
 * constraints and `(*)` for subtrees already rendered elsewhere.
 */
abstract class DependenciesReport : DefaultTask() {

    @get:Input
    abstract val configurationName: Property<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    init {
        // The report must always reflect the current dependency graph, so never
        // treat the task as up-to-date based on its declared inputs/outputs.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generate() {
        val configuration = project.configurations.getByName(configurationName.get())
        val root = configuration.incoming.resolutionResult.rootComponent.get()
        val builder = StringBuilder()
        render(resolvedDependencies(root), "", builder, HashSet())
        val file = reportFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }

    private fun render(
        dependencies: List<ResolvedDependencyResult>,
        prefix: String,
        out: StringBuilder,
        rendered: MutableSet<String>,
    ) {
        dependencies.forEachIndexed { index, dependency ->
            val isLast = index == dependencies.lastIndex
            val childPrefix = prefix + if (isLast) "     " else "|    "

            val selected = dependency.selected
            val id = componentId(selected)
            val repeated = id in rendered

            out.append(prefix)
            out.append(if (isLast) "\\--- " else "+--- ")
            out.append(labelOf(dependency))
            if (dependency.isConstraint) out.append(" (c)")
            if (repeated) {
                out.append(" (*)")
            }
            out.append('\n')
            if (!repeated) {
                rendered.add(id)
                render(resolvedDependencies(selected), childPrefix, out, rendered)
            }
        }
    }
}

private fun resolvedDependencies(component: ResolvedComponentResult): List<ResolvedDependencyResult> =
    component.dependencies
        .filterIsInstance<ResolvedDependencyResult>()
        .sortedWith(compareBy { dependencySortKey(it) })

private fun labelOf(dependency: ResolvedDependencyResult): String =
    when (val requested = dependency.requested) {
        is ProjectComponentSelector -> "project ${requested.projectPath}"
        is ModuleComponentSelector -> {
            val selectedVersion = dependency.selected.moduleVersion?.version
            val base = "${requested.group}:${requested.module}"
            when {
                requested.version.isEmpty() && selectedVersion != null ->
                    "$base -> $selectedVersion"
                requested.version.isEmpty() -> base
                selectedVersion != null && selectedVersion != requested.version ->
                    "$base:${requested.version} -> $selectedVersion"
                else -> "$base:${requested.version}"
            }
        }
        else -> requested.displayName
    }

private fun componentId(component: ResolvedComponentResult): String =
    component.moduleVersion?.let { "${it.group}:${it.name}" } ?: component.id.displayName

private fun dependencySortKey(dependency: ResolvedDependencyResult): String =
    when (val requested = dependency.requested) {
        is ProjectComponentSelector -> "0${requested.projectPath}"
        is ModuleComponentSelector -> "1${requested.group}:${requested.module}:${requested.version}"
        else -> "2${requested.displayName}"
    }
