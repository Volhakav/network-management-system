package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val topology = loadTopology()
    log.info("Loaded ${topology.devices.size} devices, ${topology.connections.size} connections")

    routing {
        get("/") {
            call.respondText("Network management system is running")
        }
        // TEMPORARY: only to check in the browser that the data was loaded
        get("/debug/devices") {
            call.respond(topology.devices)
        }
    }
}