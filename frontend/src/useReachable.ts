import { useEffect, useState } from 'react'
import type { ReachabilityEvent } from './types'

export function useReachable(deviceId: number | null) {
    const [reachable, setReachable] = useState<Set<number>>(new Set())
    const [connected, setConnected] = useState(false)

    useEffect(() => {
        setReachable(new Set())
        if (deviceId === null) return

        const source = new EventSource(`/devices/${deviceId}/reachable-devices`)
        source.onopen = () => setConnected(true)
        source.onerror = () => setConnected(false)
        source.onmessage = (msg) => {
            const event: ReachabilityEvent = JSON.parse(msg.data)
            setReachable((prev) => {
                switch (event.type) {
                    case 'INITIAL_STATE':
                        return new Set(event.deviceIds)
                    case 'ADDED':
                        return new Set(prev).add(event.deviceId)
                    case 'REMOVED': {
                        const next = new Set(prev)
                        next.delete(event.deviceId)
                        return next
                    }
                }
            })
        }

        return () => {
            source.close()
            setConnected(false)
        }
    }, [deviceId])

    return { reachable, connected }
}