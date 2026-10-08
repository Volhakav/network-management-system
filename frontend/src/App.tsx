import { useEffect, useState } from 'react'
import { fetchTopology, setActive } from './api'
import { NetworkView } from './NetworkView'
import { useReachable } from './useReachable'
import type { Topology } from './types'

export default function App() {
    const [topology, setTopology] = useState<Topology | null>(null)
    const [subscribedId, setSubscribedId] = useState<number | null>(null)
    const [error, setError] = useState<string | null>(null)
    const { reachable, connected } = useReachable(subscribedId)

    useEffect(() => {
        fetchTopology().then(setTopology).catch((e) => setError(String(e)))
    }, [])

    const handleToggle = async (id: number, active: boolean) => {
        try {
            await setActive(id, active)
            setTopology((t) =>
                t && { ...t, devices: t.devices.map((d) => (d.id === id ? { ...d, active } : d)) }
            )
        } catch (e) {
            setError(String(e))
        }
    }

    if (error) return <p>Error: {error}</p>
    if (!topology) return <p>Loading…</p>

    return (
        <div>
            <h1>Network Management System</h1>

            <div className="controls">
                <label className="select-label" htmlFor="city">Subscribe to:</label>
                <select
                    id="city"
                    className="city-select"
                    value={subscribedId ?? ''}
                    onChange={(e) => setSubscribedId(e.target.value === '' ? null : Number(e.target.value))}
                >
                    <option value="">(none)</option>
                    {topology.devices.map((d) => (
                        <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                </select>

                {subscribedId !== null && (
                    <span className="status">
            {connected ? 'connected' : 'connecting…'}, reachable: {reachable.size}
          </span>
                )}
            </div>

            <p>Click a device to turn it on or off.</p>
            <NetworkView
                topology={topology}
                subscribedId={subscribedId}
                reachable={reachable}
                onToggle={handleToggle}
            />
        </div>
    )
}