import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

// apollo client

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
import {PrimeReactProvider} from "primereact/api";
import GraphList from "./component/GraphList.tsx";
import App from "./component/App.tsx";
import GraphEditor from "./component/GraphEditor.tsx";
import {getGraphQlUrl} from "./component/Util.tsx";
import {ApolloProvider, HttpLink, ApolloClient, from, InMemoryCache} from "@apollo/client";

const link = new HttpLink({
    uri: getGraphQlUrl(),
});

const client = new ApolloClient({cache: new InMemoryCache(), link: link});

const router = createBrowserRouter([
    {
        path: "/",
        element: <App />,
        children: [
            {
                path: 'list',
                element: <GraphList />
            },
            {
                path: 'editor',
                element: <GraphEditor />
            }
        ]
    },
])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <ApolloProvider client={client}>
          <PrimeReactProvider>
                <RouterProvider router={router} />
          </PrimeReactProvider>
      </ApolloProvider>
  </StrictMode>,
)
