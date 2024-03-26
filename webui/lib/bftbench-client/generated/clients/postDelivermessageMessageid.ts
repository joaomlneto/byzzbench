import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { PostDelivermessageMessageidMutationResponse, PostDelivermessageMessageidPathParams } from "../types/PostDelivermessageMessageid";

 /**
     * @summary Deliver a message
     * @link /deliverMessage/:messageId */
export async function postDelivermessageMessageid(messageId: PostDelivermessageMessageidPathParams["messageId"], options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<PostDelivermessageMessageidMutationResponse>> {
    const res = await client<PostDelivermessageMessageidMutationResponse>({
        method: "post",
        url: `/deliverMessage/${messageId}`,
        ...options
    });
    return res;
}