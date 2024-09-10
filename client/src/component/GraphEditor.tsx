import React from 'react';
import {useCallback, useState} from 'react'
import {addEdge, Background, Controls, ReactFlow, useEdgesState, useNodesState} from "@xyflow/react";
import {Outlet, useOutletContext} from "react-router-dom";
import {Button} from "primereact/button";
import {useGraph} from "./App.tsx";

const initialNodes = [
    { id: '1', position: { x: 0, y: 0 }, data: { label: '1' } },
    { id: '2', position: { x: 0, y: 100 }, data: { label: '2' } },
];
const initialEdges = [{ id: 'e1-2', source: '1', target: '2', type: 'step' }];

/*
* todo: custom nodes, layout libraries
*
* */
export default function GraphEditor() {
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
    const [graphId, setGraphId] = useGraph();

    const onConnect = useCallback(
        (params) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    return (
        <>
            <div style={{ width: '100vw', height: '100vh' }}>
                {graphId}
                <Button label={'test'} onClick={() => setGraphId(100)}></Button>
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                >
                    <Controls />
                    <Background variant="dots" gap={12} size={1} />
                </ReactFlow>
            </div>
        </>
    )
}