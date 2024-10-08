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
import {useLazyQuery, useMutation, useQuery} from "@apollo/client";
import {ALL_GRAPHS, CREATE_GRAPH, DELETE_GRAPH, EXECUTE_GRAPH} from "../query/Queries.ts";
import {useNavigate} from "react-router-dom";

export default function GraphList() {
    const [graphId, setGraphId, toast, setActiveIndex, graphName, setGraphName] = useGraph();
    const navigate = useNavigate();
    const [selectedGraph, setSelectedGraph] = useState(null);

    const [ createGraphQuery ] = useMutation(CREATE_GRAPH)
    const [ deleteGraphQuery ] = useMutation(DELETE_GRAPH)
    const [ executeGraphQuery ] = useLazyQuery(EXECUTE_GRAPH)
    const allGraphsQuery = useQuery(ALL_GRAPHS);


    const [isCreateDialogVisible, setCreatedDialogVisible] = useState(false);
    const [isDeleteGraphDialogVisible, setDeleteGraphDialogVisible] = useState(false);
    const [isExecutionDialogVisible, setExecutionDialogVisible] = useState(false)

    const defaultValues = {
        name: ''
    }
    const { control, formState: { errors }, handleSubmit, reset, register } = useForm({ defaultValues });

    const deleteGraph = () => {
        deleteGraphQuery({ variables: { graphId: selectedGraph.id }}).then(it => {
            toast.current.show({ severity: 'success', summary: 'Graph deleted', detail: "" });
            allGraphsQuery.refetch();
        }).catch(err => {
            console.log(err)
            toast.current.show({severity: 'error', summary: 'Graph deleting error', detail: ""});
        })
        setDeleteGraphDialogVisible(false);
        setSelectedGraph(null);
    }

    const executeGraph = (graphId: number) => {
        executeGraphQuery({ variables: { graphId }}).then(it => {
            console.log(it)
            toast.current.show({ severity: 'success', summary: `Execution successfully started`, detail: "" });
        }).catch(err => {
            console.log(err)
            toast.current.show({severity: 'error', summary: 'Graph execution error', detail: ""});
        })
    }

    const actionBodyTemplate = (rowData) => {
        return (
            <Fragment>
                <Button icon="pi pi-pencil" rounded outlined className="mr-2 action-button" onClick={event => {
                    setGraphId(rowData.id)
                    setGraphName(rowData.name)
                    setActiveIndex(1)
                    navigate("/editor")
                }} />
                <Button icon="pi pi-trash" rounded outlined severity="danger" className="mr-2 action-button" onClick={() => {
                    setSelectedGraph(rowData)
                    setDeleteGraphDialogVisible(true)
                }} />
                <Button icon="pi pi-play" rounded outlined severity="success" className="mr-2 action-button" onClick={() => {
                    executeGraph(+rowData.id)
                }} />
                <Button icon="pi pi-bars" outlined rounded severity={'info'} className="action-button" onClick={() => {
                    setSelectedGraph(rowData)
                    setExecutionDialogVisible(true)
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
        createGraphQuery({ variables: { input: {name: data.name, edges: data.edges ?? [], nodes: data.nodes ?? []}}})
            .then(graph => {
                toast.current.show({ severity: 'success', summary: 'Graph Created', detail: `${graph.data.createGraph.name} created` });
                allGraphsQuery.refetch();
            })
            .catch(err => {
                toast.current.show({ severity: 'error', summary: 'Error creating graph', detail: ""})
            });
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
            
            <Dialog header={'Execution list'} visible={isExecutionDialogVisible} onHide={() => setExecutionDialogVisible(false)}>
                <DataTable value={allGraphsQuery.data && selectedGraph ? allGraphsQuery.data.allGraphs.find(it => it.id === selectedGraph.id).executions : null} dataKey={'id'} lazy loading={allGraphsQuery.loading}>
                    <Column field={'id'} header={'Id'} />
                    <Column field={'startTime'} header={'Start Time'} />
                    <Column field={'endTime'} header={'End Time'} />
                    <Column field={'status'} header={'Status'} />
                </DataTable>
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
