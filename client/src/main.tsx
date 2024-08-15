import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './index.css'
// react flow
import '@xyflow/react/dist/style.css'
// primeflex
import '/node_modules/primeflex/primeflex.css'
// primeicons
import 'primeicons/primeicons.css'
// primereact
import 'primereact/resources/primereact.min.css'
import 'primereact/resources/themes/md-light-indigo/theme.css'

import {createBrowserRouter, RouterProvider} from "react-router-dom";

const router = createBrowserRouter([
    {
        path: "/",
        element: <App />
    }
])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
