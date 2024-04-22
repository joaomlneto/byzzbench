/**
 * @description The status of the message delivery
*/
export type PostMessageMessageidDeliver200 = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 /**
 * @description The status of the message delivery
*/
export type PostMessageMessageidDeliverMutationResponse = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 export type PostMessageMessageidDeliverPathParams = {
    /**
     * @description The ID of the message to deliver
     * @type string
    */
    messageId: string;
};
export type PostMessageMessageidDeliverMutation = {
    Response: PostMessageMessageidDeliverMutationResponse;
    PathParams: PostMessageMessageidDeliverPathParams;
};