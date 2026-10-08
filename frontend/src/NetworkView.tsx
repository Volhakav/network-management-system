import type { Topology } from './types'

type Props = {
    topology: Topology
    subscribedId: number | null
    reachable: Set<number>
    onToggle: (id: number, active: boolean) => void
}

const SIZE = 840
const CENTER = SIZE / 2
const RADIUS = 270
const LABEL_OFFSET = 30

type Pos = { x: number; y: number; lx: number; ly: number; anchor: 'start' | 'middle' | 'end' }

export function NetworkView({ topology, subscribedId, reachable, onToggle }: Props) {
    const { devices, connections } = topology

    const position = new Map<number, Pos>()
    devices.forEach((d, i) => {
        const angle = (2 * Math.PI * i) / devices.length - Math.PI / 2
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        position.set(d.id, {
            x: CENTER + RADIUS * cos,
            y: CENTER + RADIUS * sin,
            lx: CENTER + (RADIUS + LABEL_OFFSET) * cos,
            ly: CENTER + (RADIUS + LABEL_OFFSET) * sin + 5,
            anchor: cos > 0.3 ? 'start' : cos < -0.3 ? 'end' : 'middle',
        })
    })
    const byId = new Map(devices.map((d) => [d.id, d]))

    const colorOf = (id: number, active: boolean) => {
        if (id === subscribedId) return '#3b82f6' // blue: subscribed
        if (!active) return '#ef4444'             // red: switched off
        if (subscribedId === null) return '#9ca3af'
        return reachable.has(id) ? '#22c55e' : '#9ca3af' // green: reachable, grey: not
    }

    return (
        <svg viewBox={`0 0 ${SIZE} ${SIZE}`} style={{ maxWidth: SIZE, width: '100%' }}>
            {connections.map((c) => {
                const a = position.get(c.from)!
                const b = position.get(c.to)!
                const off = !byId.get(c.from)!.active || !byId.get(c.to)!.active
                return (
                    <line
                        key={`${c.from}-${c.to}`}
                        x1={a.x} y1={a.y} x2={b.x} y2={b.y}
                        stroke="#6b7280"
                        strokeWidth={2}
                        strokeDasharray={off ? '5 5' : undefined}
                    />
                )
            })}

            {devices.map((d) => {
                const p = position.get(d.id)!
                return (
                    <g
                        key={d.id}
                        onClick={() => onToggle(d.id, !d.active)}
                        style={{ cursor: 'pointer' }}
                    >
                        <circle cx={p.x} cy={p.y} r={17} fill={colorOf(d.id, d.active)} />
                        <text x={p.x} y={p.y + 4} textAnchor="middle" fontSize={12} fontWeight={600} fill="white">
                            {d.id}
                        </text>
                        <text
                            x={p.lx}
                            y={p.ly}
                            textAnchor={p.anchor}
                            fontSize={14}
                            fontWeight={500}
                            fill="#f3f4f6"
                            stroke="#16171d"
                            strokeWidth={3}
                            strokeLinejoin="round"
                            paintOrder="stroke"
                        >
                            {d.name}
                        </text>
                    </g>
                )
            })}
        </svg>
    )
}