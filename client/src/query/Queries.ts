import { gql } from '@apollo/client'

export const GRAPH_BY_ID = gql`
    query {
        graphById(id: "1") {
            id,
            node {
                id,
                filename
            }
        }
    }
`;