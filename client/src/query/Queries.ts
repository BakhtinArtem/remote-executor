import { gql } from '@apollo/client'

export const GRAPH_BY_ID = gql`
    query GraphById($id: ID) {
        graphById(id: $id) {
            id,
            nodes {
                id,
                filename,
                image,
                isRoot
            },
            edges {
                id,
                fromNode {
                    id
                }
                toNode {
                    id
                }
            }
        }
    }
`;

export const ALL_GRAPHS = gql`
    query AllGraphs {
        allGraphs {
            id
            name
            nodes {
                id
                filename
                image
                isRoot
            }
        }
    }
`;

export const CREATE_GRAPH = gql`
    mutation createGraph($input: GraphInput!) {
        createGraph(input: $input) {
            name
            nodes {
                id
                filename
                image
                isRoot
            }
            edges {
                id
                fromNode { id }
                toNode { id }
            }
        }
    }
`

export const UPDATE_GRAPH = gql`
    mutation updateGraph($input: GraphInput!) {
        updateGraph(input: $input) {
            name
            nodes {
                id
                filename
                image
                isRoot
            }
            edges {
                id
                fromNode { id }
                toNode { id }
            }
        }
    }
`

export const DELETE_GRAPH = gql`
    mutation deleteGraph($graphId: ID) {
        deleteGraph(graphId: $graphId)
    }
`