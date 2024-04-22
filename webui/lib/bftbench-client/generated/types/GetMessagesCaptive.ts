/**
 * @description The list of captive messages
*/
export type GetMessagesCaptive200 = {
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
 * @description The list of captive messages
*/
export type GetMessagesCaptiveQueryResponse = {
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
export type GetMessagesCaptiveQuery = {
    Response: GetMessagesCaptiveQueryResponse;
};