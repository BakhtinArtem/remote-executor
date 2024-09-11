import {useGraph} from "./App.tsx";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Fragment, useState} from "react";
import {Button} from "primereact/button";
import {Dialog} from "primereact/dialog";
import {Controller, useForm} from "react-hook-form";
import {InputText} from "primereact/inputtext";
import {parseError} from "./Util.tsx";
import {FloatLabel} from "primereact/floatlabel";
import {useMutation, useQuery} from "@apollo/client";
import {ALL_GRAPHS, CREATE_GRAPH, DELETE_GRAPH} from "../query/Queries.ts";

export default function GraphList() {
    const [graphId, setGraphId, toast] = useGraph();
    const [selectedGraph, setSelectedGraph] = useState(null);

    const [ createGraphQuery ] = useMutation(CREATE_GRAPH)
    const [ deleteGraphQuery ] = useMutation(DELETE_GRAPH)
    const allGraphsQuery = useQuery(ALL_GRAPHS);


    const [isCreateDialogVisible, setCreatedDialogVisible] = useState(false);
    const [isDeleteGraphDialogVisible, setDeleteGraphDialogVisible] = useState(false);

    const defaultValues = {
        name: ''
    }
    const { control, formState: { errors }, handleSubmit, reset, register } = useForm({ defaultValues });

    const deleteGraph = () => {
        console.log(selectedGraph)
        deleteGraphQuery({ variables: { graphId: selectedGraph.id }}).then(it => {
            toast.current.show({ severity: 'success', summary: 'Graph Deleted', detail: "" });
            allGraphsQuery.refetch();
        }).catch(err => {
            console.log(err)
            toast.current.show({severity: 'error', summary: 'Graph Deleting Error', detail: ""});
        })
        setDeleteGraphDialogVisible(false);
        setSelectedGraph(null);
    }

    const actionBodyTemplate = (rowData) => {
        return (
            <Fragment>
                <Button icon="pi pi-pencil" rounded outlined className="mr-2 action-button" onClick={() => console.log(rowData)} />
                <Button icon="pi pi-trash" rounded outlined severity="danger" className="action-button" onClick={() => {
                    setSelectedGraph(rowData)
                    setDeleteGraphDialogVisible(true)
                }} />
            </Fragment>
        )
    };

    const deleteGraphDialogFooter = (
        <Fragment>
            <Button label="No" icon="pi pi-times" outlined onClick={() => {
                setSelectedGraph(null);
                setDeleteGraphDialogVisible(false)
            }} />
            <Button label="Yes" icon="pi pi-check" severity="danger" onClick={deleteGraph} />
        </Fragment>
    );

    const headerTemplate = () => {
        return (
            <Fragment>
                <div className="flex flex-row">
                    <h1 className="m-0">Graph List</h1>
                    <Button icon="pi pi-plus" rounded outlined className="action-button ml-3" onClick={() => setCreatedDialogVisible(true)} />
                </div>
            </Fragment>
        )
    }

    const onSubmit = (data) => {
        createGraphQuery({ variables: { input: {name: data.name, edges: data.edges ?? [], nodes: data.nodes ?? []} as GraphInput }})
            .then(graph => {
                console.log(graph)
                toast.current.show({ severity: 'success', summary: 'Graph Created', detail: `${graph.data.createGraph.name} created` });
                allGraphsQuery.refetch();
            })
            .catch(err => {
                toast.current.show({ severity: 'error', summary: 'Error creating graph', detail: ""})
            });
        // fetch('http://localhost:8080/v1/jar', { method: "GET" }).then(it => it.json()).then(it => console.log(it))
        reset();
        setCreatedDialogVisible(false);
    };

    return (
        <>
            <DataTable value={allGraphsQuery.data ? allGraphsQuery.data.allGraphs : null} dataKey={'id'} lazy loading={allGraphsQuery.loading} header={headerTemplate}>
                <Column field="name" header="Graph Name"/>
                <Column body={actionBodyTemplate} header="Actions"/>
            </DataTable>

            <Dialog header="New Graph" visible={isCreateDialogVisible} onHide={() => setCreatedDialogVisible(false)}>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="field mt-4">
                        <span className="flex flex-column">
                                <Controller name="name" control={control} rules={{required: true}} render={({ field, formState }) => (
                                    <>
                                        <FloatLabel>
                                            <label htmlFor={field.name}>Graph Name</label>
                                            <InputText keyfilter="alpha" id={field.name} {...field} autoFocus />
                                        </FloatLabel>
                                        {field && formState && parseError(formState, field.name)}
                                    </>
                                )} />
                        </span>
                    </div>
                    <Button type="submit" label="Submit" className="mt-2"/>
                </form>
            </Dialog>

            <Dialog visible={isDeleteGraphDialogVisible} style={{ width: '32rem' }} breakpoints={{ '960px': '75vw', '641px': '90vw' }}
                    header="Confirm" modal footer={deleteGraphDialogFooter} onHide={() => setDeleteGraphDialogVisible(false)}>
                <div className="confirmation-content">
                    <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                    {selectedGraph && <span>Are you sure you want to delete the selected graph?</span>}
                </div>
            </Dialog>
        </>
    )
}
