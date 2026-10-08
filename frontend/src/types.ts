export type Device = { id: number; name: string; active: boolean }
export type Connection = { from: number; to: number }
export type Topology = { devices: Device[]; connections: Connection[] }

export type ReachabilityEvent =
    | { type: 'INITIAL_STATE'; deviceIds: number[] }
    | { type: 'ADDED'; deviceId: number }
    | { type: 'REMOVED'; deviceId: number }