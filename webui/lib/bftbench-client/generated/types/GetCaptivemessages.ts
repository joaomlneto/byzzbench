/**
 * @description The list of captive messages
*/
export type GetCaptivemessages200 = {
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
export type GetCaptivemessagesQueryResponse = {
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
export type GetCaptivemessagesQuery = {
    Response: GetCaptivemessagesQueryResponse;
};