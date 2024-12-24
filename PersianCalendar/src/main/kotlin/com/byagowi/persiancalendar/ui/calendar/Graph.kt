package com.byagowi.persiancalendar.ui.calendar

// These aren't the most optimized implementations, probably going to be replaced with
// better one in the future though they should be enough for our use
class Graph(verticesCount: Int) {
    private val adjacency = List(verticesCount) { mutableSetOf<Int>() }

    override fun toString(): String = adjacency.toString()

    fun addEdge(x: Int, y: Int) {
        adjacency[x].add(y)
        adjacency[y].add(x)
    }

    // DFS isn't the most optimized implementation but should serve us well for our use
    // https://www.geeksforgeeks.org/connected-components-in-an-undirected-graph/
    fun connectedComponents(): Sequence<List<Int>> = sequence {
        val visited = BooleanArray(adjacency.size)
        adjacency.indices.forEach { if (!visited[it]) yield(sequence { dfs(it, visited) }) }
    }.map { it.toList() }

    private suspend fun SequenceScope<Int>.dfs(v: Int, visited: BooleanArray) {
        visited[v] = true
        yield(v)
        adjacency[v].forEach { if (!visited[it]) dfs(it, visited) }
    }

    // Greedy coloring isn't going to get us the most optimized coloring but the problem is
    // considered being NP-Complete so even an approximation should be enough for the use.
    // https://www.geeksforgeeks.org/graph-coloring-set-2-greedy-algorithm/
    fun colors(): List<Int> {
        if (adjacency.isEmpty()) return emptyList()
        val result = MutableList(adjacency.size) { -1 }
        result[0] = 0
        val available = BooleanArray(adjacency.size)
        adjacency.indices.drop(1).forEach { x ->
            adjacency[x].forEach { if (result[it] != -1) available[result[it]] = true }
            result[x] = adjacency.indices.firstOrNull { !available[it] } ?: adjacency.size
            available.fill(false)
        }
        return result
    }
}
