import {Outlet, useNavigate, useOutletContext} from "react-router-dom";
import {TabMenu} from "primereact/tabmenu";
import {MutableRefObject, useRef, useState} from "react";
import {Toast} from "primereact/toast";

type ContextType = [ number | undefined, (number) => {}, MutableRefObject<Toast> ];

export default function App() {
    const navigate = useNavigate();
    const [graphId, setGraphId] = useState<number>();
    const toast = useRef(null);
    const items = [
        { label: 'Graph List', icon: 'pi pi-home', command: () => navigate('/list')  },
        { label: 'Graph', icon: 'pi pi-sitemap ', command: () => navigate('/editor') },
    ];

    return (
        <>
            <Toast ref={toast} />
            <TabMenu model={items} />
            <Outlet context={ [graphId, setGraphId, toast] }/>
        </>
    )
}

export function useGraph() {
    return useOutletContext<ContextType>();
}