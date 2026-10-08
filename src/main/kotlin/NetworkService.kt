package com.example

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Subscriber(
    val start: Int,
    var known: Set<Int>,
    val events: Channel<ReachabilityEvent>
)

fun diff(old: Set<Int>, new: Set<Int>): List<ReachabilityEvent> =
    (old - new).sorted().map { ReachabilityEvent.Removed(it) } +
            (new - old).sorted().map { ReachabilityEvent.Added(it) }

class NetworkService(
    private val graph: NetworkGraph,
    private val topology: Topology
) {

    private val mutex = Mutex()
    private val subscribers = mutableSetOf<Subscriber>()

    suspend fun subscribe(deviceId: Int): Subscriber? = mutex.withLock {
        if (!graph.exists(deviceId)) return@withLock null

        val reachable = graph.reachableFrom(deviceId)
        val subscriber = Subscriber(deviceId, reachable, Channel(Channel.UNLIMITED))
        subscriber.events.trySend(ReachabilityEvent.InitialState(reachable.sorted()))
        subscribers.add(subscriber)
        subscriber
    }

    suspend fun unsubscribe(subscriber: Subscriber) = mutex.withLock {
        subscribers.remove(subscriber)
        subscriber.events.close()
    }

    suspend fun setActive(deviceId: Int, value: Boolean): Boolean = mutex.withLock {
        if (!graph.exists(deviceId)) return@withLock false

        val changed = graph.setActive(deviceId, value)
        if (changed) {
            for (subscriber in subscribers) {
                val fresh = graph.reachableFrom(subscriber.start)
                for (event in diff(subscriber.known, fresh)) {
                    subscriber.events.trySend(event)
                }
                subscriber.known = fresh
            }
        }
        true
    }

    suspend fun currentTopology(): Topology = mutex.withLock {
        Topology(
            devices = topology.devices.map { it.copy(active = graph.isActive(it.id)) },
            connections = topology.connections
        )
    }
}