import {FieldValues, UseFormStateReturn} from "react-hook-form";
import React from "react";
import {Message} from "primereact/message";

export function parseError(formState: UseFormStateReturn<FieldValues>, fieldName: string): React.JSX.Element | null {
    if (!formState.errors[fieldName]) {
        return;
    }
    let errMsg;
    switch (formState.errors[fieldName]?.type) {
        case "required":
            errMsg = 'This field is required'
            break;
        default:
            errMsg = 'Error'
            break;
    }
    return(
        <Message className='mt-1' severity="error" text={errMsg} />
    );
}

export function getApiUrl(... paths) {
    return import.meta.env['VITE_BACKEND_API'] + (paths.length > 0 ? "/" + paths.join('/') : '');
}

export function getGraphQlUrl() {
    return 'http://localhost:8080/graphql'
}