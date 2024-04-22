/**
 * @description OK
*/
export type GetNodeById200 = {
    [key: string]: object;
};

 export type GetNodeByIdPathParams = {
    /**
     * @description The ID of the node
     * @type string
    */
    nodeId: string;
};

 /**
 * @description OK
*/
export type GetNodeByIdQueryResponse = {
    [key: string]: object;
};
export type GetNodeByIdQuery = {
    Response: GetNodeByIdQueryResponse;
    PathParams: GetNodeByIdPathParams;
};