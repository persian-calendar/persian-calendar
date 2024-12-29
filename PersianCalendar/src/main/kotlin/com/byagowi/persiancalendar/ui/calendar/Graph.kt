package com.byagowi.persiancalendar.ui.calendar

// The use in this app is https://en.wikipedia.org/wiki/Interval_graph
class Graph(verticesCount: Int) {
    private val adjacency = List(verticesCount) { mutableSetOf<Int>() }

    override fun toString(): String = adjacency.toString()

    fun addEdge(x: Int, y: Int) {
        adjacency[x].add(y)
        adjacency[y].add(x)
    }

    fun connectedComponents(): List<List<Int>> {
        val visited = mutableSetOf<Int>()
        suspend fun SequenceScope<Int>.dfs(vertex: Int): Unit =
            adjacency[vertex.also { yield(it) }].forEach { if (it !in visited) dfs(it) }
        return adjacency.indices.mapNotNull {
            if (it in visited) null else sequence { dfs(it) }.onEach(visited::add).toList()
        }
    }

    // It's a simple greedy implementation, https://en.wikipedia.org/wiki/Greedy_coloring
    // readEvents applies a sort by starting point which is suggested for interval graphs
    // and a sort for putting longer events first is done in DaysScreen
    fun coloring(): List<Int> {
        val result = MutableList(adjacency.size) { -1 }
        adjacency.forEachIndexed { i, x ->
            val used = buildSet { x.forEach { if (result[it] != -1) add(result[it]) } }
            result[i] = adjacency.indices.firstOrNull { it !in used } ?: 0
        }
        return result
    }
}
