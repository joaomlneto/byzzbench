/**
 * @description The status of the message delivery
*/
export type PostMessageMessageidDrop200 = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 /**
 * @description The status of the message delivery
*/
export type PostMessageMessageidDropMutationResponse = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 export type PostMessageMessageidDropPathParams = {
    /**
     * @description The ID of the message to drop
     * @type string
    */
    messageId: string;
};
export type PostMessageMessageidDropMutation = {
    Response: PostMessageMessageidDropMutationResponse;
    PathParams: PostMessageMessageidDropPathParams;
};