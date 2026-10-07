package com.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkGraphTest {

    // chain 0-1-2-3 from the assignment
    private fun chain() = NetworkGraph(
        Topology(
            devices = (0..3).map { Device(it, "d$it", true) },
            connections = listOf(Connection(0, 1), Connection(1, 2), Connection(2, 3))
        )
    )

    private fun real() = NetworkGraph(loadTopology())

    @Test
    fun `chain - everything reachable`() {
        assertEquals(setOf(1, 2, 3), chain().reachableFrom(0))
    }

    @Test
    fun `chain - turning off 2 removes 2 and 3`() {
        val g = chain()
        g.setActive(2, false)
        assertEquals(setOf(1), g.reachableFrom(0))
    }

    @Test
    fun `inactive start gives empty set`() {
        val g = chain()
        g.setActive(0, false)
        assertTrue(g.reachableFrom(0).isEmpty())
    }

    @Test
    fun `turning a device back on restores reachability`() {
        val g = chain()
        g.setActive(2, false)
        g.setActive(2, true)
        assertEquals(setOf(1, 2, 3), g.reachableFrom(0))
    }

    @Test
    fun `scenario 1 - Lublin, turn off Kielce`() {
        val g = real()
        val before = g.reachableFrom(7)
        g.setActive(15, false)
        assertEquals(setOf(15, 16, 17, 18, 19), before - g.reachableFrom(7))
    }

    @Test
    fun `scenario 2 - Radom, turn off Wroclaw`() {
        val g = real()
        val before = g.reachableFrom(12)
        g.setActive(2, false)
        assertEquals(setOf(2), before - g.reachableFrom(12))
    }

    @Test
    fun `scenario 3 - Lublin, turn off Torun`() {
        val g = real()
        val before = g.reachableFrom(7)
        g.setActive(13, false)
        assertEquals(setOf(13), before - g.reachableFrom(7))
    }

    @Test
    fun `scenario 4 - Gdansk, turn off Sosnowiec`() {
        val g = real()
        val before = g.reachableFrom(4)
        assertEquals((0..19).toSet() - 4, before)
        g.setActive(14, false)
        assertTrue(g.reachableFrom(4).isEmpty())
    }
}