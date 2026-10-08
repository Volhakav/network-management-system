package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    val topology = loadTopology()
    log.info("Loaded ${topology.devices.size} devices, ${topology.connections.size} connections")

    val service = NetworkService(NetworkGraph(topology), topology)

    routing {
        get("/") {
            call.respondText("Network management system is running")
        }

        get("/topology") {
            call.respond(service.currentTopology())
        }

        patch("/devices/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Device id must be an integer")
                return@patch
            }

            val body = try {
                call.receive<PatchDeviceRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Body must be JSON like {\"active\": false}")
                return@patch
            }

            if (service.setActive(id, body.active)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Device $id not found")
            }
        }

        sse("/devices/{id}/reachable-devices") {
            val id = call.parameters["id"]?.toIntOrNull()
            val subscriber = id?.let { service.subscribe(it) }
            if (subscriber == null) {
                // unknown or invalid id: nothing to stream, close right away
                return@sse
            }

            try {
                for (event in subscriber.events) {
                    val json = Json.encodeToString(ReachabilityEvent.serializer(), event)
                    send(ServerSentEvent(data = json))
                }
            } finally {
                withContext(NonCancellable) { service.unsubscribe(subscriber) }
            }
        }
    }
}