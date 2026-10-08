import type { Topology } from './types'

export async function fetchTopology(): Promise<Topology> {
    const res = await fetch('/topology')
    if (!res.ok) throw new Error(`GET /topology failed: ${res.status}`)
    return res.json()
}

export async function setActive(id: number, active: boolean): Promise<void> {
    const res = await fetch(`/devices/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ active }),
    })
    if (!res.ok) throw new Error(`PATCH /devices/${id} failed: ${res.status}`)
}