package com.byagowi.persiancalendar

import androidx.collection.intListOf
import com.byagowi.persiancalendar.ui.calendar.Graph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GraphTest {
    @Test
    fun colors0() {
        val g = Graph(0)
        assertEquals(listOf(), g.colors())
    }

    @Test
    fun colors1() {
        val g = Graph(5)
        g.addEdge(0, 1)
        g.addEdge(0, 2)
        g.addEdge(1, 2)
        g.addEdge(1, 3)
        g.addEdge(2, 3)
        g.addEdge(3, 4)
        assertEquals(listOf(0, 1, 2, 0, 1), g.colors())
    }

    @Test
    fun colors2() {
        val g = Graph(5)
        g.addEdge(0, 1)
        g.addEdge(0, 2)
        g.addEdge(1, 2)
        g.addEdge(1, 4)
        g.addEdge(2, 4)
        g.addEdge(4, 3)
        assertEquals(listOf(0, 1, 2, 0, 3), g.colors())
    }

    @Test
    fun connectedComponents() {
        val g = Graph(5)
        g.addEdge(1, 0)
        g.addEdge(2, 1)
        g.addEdge(3, 4)
        assertEquals(listOf(intListOf(0, 1, 2), intListOf(3, 4)), g.connectedComponents().toList())
        assertEquals(listOf(0, 1, 0, 0, 1), g.colors())
    }
}
