import {Outlet, useNavigate, useOutletContext} from "react-router-dom";
import {TabMenu} from "primereact/tabmenu";
import {useState} from "react";

type ContextType = [ number | undefined, (number) => {} ];

export default function App() {
    const navigate = useNavigate();
    const [graphId, setGraphId] = useState<number>();
    const items = [
        { label: 'Graph List', icon: 'pi pi-home', command: () => navigate('/list')  },
        { label: 'Graph', icon: 'pi pi-sitemap ', command: () => navigate('/editor') },
    ];

    return (
        <>
            <TabMenu model={items} />
            <Outlet context={ [graphId, setGraphId] }/>
        </>
    )
}

export function useGraph() {
    return useOutletContext<ContextType>();
}