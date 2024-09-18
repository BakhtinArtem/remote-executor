import {memo} from "react";
import {Handle, Position} from "@xyflow/react";
import {InputText} from "primereact/inputtext";
import {ToggleButton} from "primereact/togglebutton";
import {Dropdown} from "primereact/dropdown";
import {FileUpload} from "primereact/fileupload";
import {Checkbox} from "primereact/checkbox";

// @ts-ignore
export default memo(({data, isConnectable}) => {
    return (
        <div className={'flex flex-column gap-1 justify-content-center p-3'} style={{ background: 'white', color: 'black', border: '1px solid #777'}}>
            <Handle
                type="target"
                position={Position.Top}
                style={{ background: '#555', width: '1em', height: '1em' }}
                onConnect={(params) => console.log('handle onConnect', params)}
                isConnectable={isConnectable}
            />
            <label htmlFor={'image-' + data.id}>Docker image:</label>
            <InputText id={'image-' + data.id} placeholder={'Image name'} value={data.image} onChange={data.onChange} disabled={!data.checked}/>
            <label htmlFor={'selectedJar-' + data.id}>JAR name:</label>
            <div className="p-inputgroup flex-1">
                {/* fixme: repeatedly loading same file */}
                <FileUpload id={'upload-' + data.id} disabled={!data.checked} auto={true} className={'file-upload-button'} mode="basic" customUpload uploadHandler={data.uploadHandler} maxFileSize={1000000} />
                <Dropdown id={'selectedJar-' + data.id} loading={!data.jars} disabled={!data.checked} options={data.jars} value={data.selectedJar} optionLabel={'name'} onChange={data.onChange}/>
            </div>
            <div className="flex align-items-center">
                { data.isRoot }
                <Checkbox id={'isRoot-' + data.id} onChange={data.onChange} disabled={!data.checked} checked={data.isRoot}></Checkbox>
                <label htmlFor={'isRoot-' + data.id} className="ml-2">Is Root Node</label>
            </div>
            <ToggleButton id={'checked-' + data.id} onLabel="Save" offLabel="Edit" onIcon="pi pi-check" offIcon="pi pi-pencil"
                          checked={data.checked} onChange={data.onChange} />
            <Handle
                type="source"
                position={Position.Bottom}
                style={{ background: '#555',  width: '1em', height: '1em' }}
                onConnect={(params) => console.log('handle onConnect', params)}
                isConnectable={isConnectable}
            />
        </div>
    );
})