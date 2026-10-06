package com.example

import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun loadTopology(resourceName: String = "topology.json"): Topology {
    val stream = Topology::class.java.classLoader.getResourceAsStream(resourceName)
        ?: error("File $resourceName not found in resources")
    val text = stream.bufferedReader().use { it.readText() }
    val topology = json.decodeFromString(Topology.serializer(), text)
    validate(topology)
    return topology
}

private fun validate(topology: Topology) {
    val ids = topology.devices.map { it.id }
    require(ids.size == ids.toSet().size) { "Device ids are not unique" }

    val known = ids.toSet()
    for (c in topology.connections) {
        require(c.from in known && c.to in known) {
            "Connection ${c.from}-${c.to} references a non-existent device"
        }
    }
}