/**
 * @description The list of dropped messages
*/
export type GetMessagesDelivered200 = {
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
export type GetMessagesDeliveredQueryResponse = {
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
export type GetMessagesDeliveredQuery = {
    Response: GetMessagesDeliveredQueryResponse;
};