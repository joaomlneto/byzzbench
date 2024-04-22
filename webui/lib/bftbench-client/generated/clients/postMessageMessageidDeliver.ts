import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { PostMessageMessageidDeliverMutationResponse, PostMessageMessageidDeliverPathParams } from "../types/PostMessageMessageidDeliver";

 /**
     * @summary Deliver a message
     * @link /message/:messageId/deliver */
export async function postMessageMessageidDeliver(messageId: PostMessageMessageidDeliverPathParams["messageId"], options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<PostMessageMessageidDeliverMutationResponse>> {
    const res = await client<PostMessageMessageidDeliverMutationResponse>({
        method: "post",
        url: `/message/${messageId}/deliver`,
        ...options
    });
    return res;
}