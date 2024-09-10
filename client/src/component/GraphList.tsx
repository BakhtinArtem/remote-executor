import {useGraph} from "./App.tsx";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Fragment, useState} from "react";
import {Button} from "primereact/button";
import {Dialog} from "primereact/dialog";
import {Controller, useForm} from "react-hook-form";
import {InputText} from "primereact/inputtext";
import {getApiUrl, getGraphQlUrl, parseError} from "./Util.tsx";
import {FloatLabel} from "primereact/floatlabel";
import {useLazyQuery, useQuery} from "@apollo/client";
import {GRAPH_BY_ID} from "../query/Queries.ts";

export default function GraphList() {
    const [graphId, setGraphId] = useGraph();
    const [ getGraph, { loading, error, data } ] = useLazyQuery(GRAPH_BY_ID);
    const [isCreateDialogVisible, setCreatedDialogVisible] = useState(false);
    const defaultValues = {
        name: ''
    }
    const { control, formState: { errors }, handleSubmit, reset, register } = useForm({ defaultValues });

    const testData = [
        { id: 1, name: "Test Graph 1"},
        { id: 2, name: "Test Graph 2"}
    ];


    const actionBodyTemplate = () => {
        return (
            <Fragment>
                <Button icon="pi pi-pencil" rounded outlined className="mr-2 action-button" onClick={() => console.log("edit product")} />
                <Button icon="pi pi-trash" rounded outlined severity="danger" className="action-button" onClick={() => console.log("delete product")} />
            </Fragment>
        )
    };

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
        const newGraph: GraphInput = {
            name: data['name'],
            nodes: [],
            edges: []
        }
        getGraph().then(it => console.log(it));
        fetch('http://localhost:8080/v1/jar', { method: "GET" }).then(it => it.json()).then(it => console.log(it))
        // env
        reset();
    };

    return (
        <>
            <DataTable value={testData} header={headerTemplate}>
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
        </>
    )
}
