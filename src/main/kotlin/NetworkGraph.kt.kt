package com.example

class NetworkGraph(topology: Topology) {

    private val adjacency: Map<Int, Set<Int>>

    private val active: MutableMap<Int, Boolean>

    init {
        val adj = topology.devices.associate { it.id to mutableSetOf<Int>() }
        for (c in topology.connections) {
            adj.getValue(c.from).add(c.to)
            adj.getValue(c.to).add(c.from)   // connections are bidirectional
        }
        adjacency = adj
        active = topology.devices.associate { it.id to it.active }.toMutableMap()
    }

    fun exists(id: Int): Boolean = id in adjacency

    fun isActive(id: Int): Boolean = active.getValue(id)

    fun setActive(id: Int, value: Boolean): Boolean {
        val old = active.getValue(id)
        active[id] = value
        return old != value
    }

    fun reachableFrom(start: Int): Set<Int> {
        if (!isActive(start)) return emptySet()

        val visited = mutableSetOf(start)
        val stack = ArrayDeque<Int>()
        stack.addLast(start)

        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            for (neighbor in adjacency.getValue(current)) {
                if (neighbor in visited) continue
                if (!isActive(neighbor)) continue
                visited.add(neighbor)
                stack.addLast(neighbor)
            }
        }

        visited.remove(start)
        return visited
    }
}