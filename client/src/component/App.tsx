import {Outlet, useNavigate, useOutletContext} from "react-router-dom";
import {TabMenu} from "primereact/tabmenu";
import {MutableRefObject, useRef, useState} from "react";
import {Toast} from "primereact/toast";

type ContextType = [ number | undefined, (number) => {}, MutableRefObject<Toast>, (number) => {}, string, (string) => {} ];

export default function App() {
    const navigate = useNavigate();
    const [graphId, setGraphId] = useState<number>();
    const [graphName, setGraphName] = useState<string>()
    const toast = useRef(null);
    const [activeIndex, setActiveIndex] = useState(0);
    const items = [
        { label: 'Graph List', icon: 'pi pi-home', command: () => navigate('/list')  },
        { label: 'Graph', icon: 'pi pi-sitemap ', command: () => navigate('/editor') },
    ];

    return (
        <>
            <Toast ref={toast} />
            <TabMenu model={items} activeIndex={activeIndex} onTabChange={(e) => setActiveIndex(e.index)} />
            <Outlet context={ [graphId, setGraphId, toast, setActiveIndex, graphName, setGraphName] }/>
        </>
    )
}

export function useGraph() {
    return useOutletContext<ContextType>();
}