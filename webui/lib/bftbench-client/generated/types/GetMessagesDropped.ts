/**
 * @description The list of dropped messages
*/
export type GetMessagesDropped200 = {
    /**
     * @description The ID of the captive message
     * @type string | undefined
    */
    id?: string;
    /**
     * @description The status of the captive message
     * @type string | undefined
    */
    status?: string;
}[];

 /**
 * @description The list of dropped messages
*/
export type GetMessagesDroppedQueryResponse = {
    /**
     * @description The ID of the captive message
     * @type string | undefined
    */
    id?: string;
    /**
     * @description The status of the captive message
     * @type string | undefined
    */
    status?: string;
}[];
export type GetMessagesDroppedQuery = {
    Response: GetMessagesDroppedQueryResponse;
};