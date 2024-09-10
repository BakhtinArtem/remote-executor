interface GraphInput {
    name: string,
    nodes: NodeInput[],
    edges: EdgeInput[]
}

interface NodeInput {
    id: number
    filename: string
    image: string
    isRoot: boolean
}

interface EdgeInput {
    from: number
    to: number
}