/**
 * @description The status of the message delivery
*/
export type PostDelivermessageMessageid200 = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 /**
 * @description The status of the message delivery
*/
export type PostDelivermessageMessageidMutationResponse = {
    /**
     * @description The status of the message delivery
     * @type string | undefined
    */
    status?: string;
};

 export type PostDelivermessageMessageidPathParams = {
    /**
     * @description The ID of the message to deliver
     * @type string
    */
    messageId: string;
};
export type PostDelivermessageMessageidMutation = {
    Response: PostDelivermessageMessageidMutationResponse;
    PathParams: PostDelivermessageMessageidPathParams;
};