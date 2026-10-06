package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: Int,
    val name: String,
    val active: Boolean
)

@Serializable
data class Connection(
    val from: Int,
    val to: Int
)

@Serializable
data class Topology(
    val devices: List<Device>,
    val connections: List<Connection>
)

@Serializable
data class PatchDeviceRequest(
    val active: Boolean
)

@Serializable
sealed interface ReachabilityEvent {

    @Serializable
    @SerialName("INITIAL_STATE")
    data class InitialState(val deviseIds: List<Int>) : ReachabilityEvent

    @Serializable
    @SerialName("ADDED")
    data class Added(val deviceId: Int) : ReachabilityEvent

    @Serializable
    @SerialName("REMOVED")
    data class Removed(val deviceId: Int) : ReachabilityEvent
}