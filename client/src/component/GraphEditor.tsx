import React, {useEffect, useState} from 'react';
import {useCallback} from 'react'
import {
    addEdge,
    Background,
    Controls,
    ReactFlow,
    ReactFlowProvider,
    useEdgesState,
    useNodesState,
    useReactFlow
} from "@xyflow/react";
import {useGraph} from "./App.tsx";
import TaskNode from "../node/TaskNode.tsx";
import {Button} from "primereact/button";
import {useNavigate} from "react-router-dom";
import {useLazyQuery, useMutation, useQuery} from "@apollo/client";
import {ALL_GRAPHS, CREATE_GRAPH, GRAPH_BY_ID, UPDATE_GRAPH} from "../query/Queries.ts";

/*
* todo: layout libraries
* */
function GraphEditor() {

    let id = 1;
    const nodeTypes = { taskNode: TaskNode };


    const onChange = (event) => {
        setNodes((nodes) => nodes.map(node => {
            const isTargetNode = node.id == event.target.id.split('-')[1];
            if (isTargetNode) {
                const elementType = event.target.id.split('-')[0]
                const value = event.target.value ?? event.checked;
                switch (elementType) {
                    default:
                        node.data[elementType] = value  //  example: node.data['selectedJar'] = value
                }
                return { ... node, data: { ... node.data }} // recreate object
            }
            return node;
        }))
    }

    const uploadHandler = async (event) => {
        const formData  = new FormData();
        const file = event.files[0];
        let blob = await fetch(file.objectURL).then((r) => r.blob());
        formData.append("jar", blob)
        formData.append("fileName", file.name)
        fetch('http://localhost:8080/v1/jar', { method: "POST", body: formData  })
            .then(() => toast.current.show({ severity: 'success', summary: 'File uploaded', detail: `${file.name} successfully uploaded` }))
            .catch(() => toast.current.show({severity: 'error', summary: 'File uploading error', detail: `${file.name} failed to upload`}))
            //  reload all nodes
            .finally(() => {
                setNodes((nodes) => nodes.map(node => ({...node, data: {...node.data}})))
                fetchJars()
            } )
    }

    const [graphId, setGraphId, toast,setActiveIndex, graphName, setGraphName] = useGraph();
    const [graphByIdQuery] = useLazyQuery(GRAPH_BY_ID, { fetchPolicy: 'no-cache', variables: { id: graphId }})


    const initialNodes = [
        { id: '' + id, position: { x: 0, y: 0 }, data: { id: '' + id, label: `Node ${id}`, checked: false, jars: [], selectedJar: '', image: '', isRoot: false, uploadHandler, onChange }, type: 'taskNode' },
    ];


    const getId = () => `${++id}`;
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const [ updateGraphQuery ] = useMutation(UPDATE_GRAPH)
    const { screenToFlowPosition } = useReactFlow();
    const navigate = useNavigate();

    useEffect(() => {
        graphByIdQuery().then(it => {
            if (!it.data || !('graphById' in it.data)) { return }   //  no graph exists
            if (it.data.graphById.nodes.length === 0 && it.data.graphById.edges.length === 0) { return }   //  empty graph
            const data = it.data.graphById
            setNodes(() => data.nodes.map((node) => {
                return {
                    id: node.id,
                    position: {
                        x: 0,
                        y: 0,
                    },
                    data: { id: node.id, label: `Node ${node.id}`, checked: false, jars: [], isRoot: node.isRoot, selectedJar: {
                            name: node.filename
                        }, image: node.image, uploadHandler, onChange },
                    type: 'taskNode',
                    origin: [0.5, 0.0],
                };
            }))
            setEdges(() => data.edges.map((edge) => {
                return {
                    id: edge.id,
                    source: edge.fromNode.id,
                    target: edge.toNode.id
                }
            }))
            fetchJars()
        })
    }, []);

    useEffect(() => {
        if (!graphId) { //  redirect to graph list if no graph is chosen for editing
            setActiveIndex(0)
            toast.current.show({ severity: 'info', summary: 'No graph is chosen', detail: ""})
            navigate('/list')
        }
    }, [])

    const fetchJars = () => fetch('http://localhost:8080/v1/jar', { method: "GET" })
        .then(it => it.json())
        .then(it => {
            setNodes((nodes) => {
                const jars = it.map(jar => ({ name: jar }))
                return nodes.map(node => {
                    return {... node, data: { ... node.data, jars}};  // rerender nodes
                })
            })
        });

    const onConnect = useCallback(
        (params) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    const onConnectEnd = useCallback(
        async (event, connectionState) => {
            // when a connection is dropped on the pane it's not valid
            if (!connectionState.isValid) {
                // we need to remove the wrapper bounds, in order to get the correct position
                const id = getId();
                const { clientX, clientY } =
                    'changedTouches' in event ? event.changedTouches[0] : event;
                const newNode = {
                    id,
                    position: screenToFlowPosition({
                        x: clientX,
                        y: clientY,
                    }),
                    data: { id, label: `Node ${id}`, checked: false, jars: [], selectedJar: '', image: '', uploadHandler, onChange },
                    type: 'taskNode',
                    origin: [0.5, 0.0],
                };
                setNodes((nodes) => [ ... nodes, newNode]);
                setEdges((eds) => eds.concat({ id, source: connectionState.fromNode.id, target: id }));
                fetchJars()
            }
        },
        [screenToFlowPosition],
    );

    const onGraphSave = () => {
        console.log(nodes)
        console.log(edges)
        const graphInput = {
            id: graphId,
            name: graphName,
            nodes: nodes.map(node => ({
                id: node.id,
                filename: node.data.selectedJar['name'],
                image: node.data.image,
                isRoot: !!node.data.isRoot
            })),
            edges: edges.map(edge => ({
                id: +edge.id,
                from: +edge.source,
                to: +edge.target
            }))
        }
        updateGraphQuery({ variables: { input: graphInput }}).then(it => console.log(it))
            .catch(err => console.log(err))
    }

    return (
        <>
            <Button className={'graph-save-button'} label={`Save '${graphName}'`} outlined onClick={onGraphSave}/>
            <div style={{ width: '100vw', height: '94vh' }}>
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    onConnectEnd={onConnectEnd}
                    nodeTypes={nodeTypes}
                >
                    <Controls />
                    <Background variant="dots" gap={12} size={1} />
                </ReactFlow>
            </div>
        </>
    )
}

export default () => (
    <ReactFlowProvider>
        <GraphEditor />
    </ReactFlowProvider>
);