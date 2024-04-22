import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { PostMessageMessageidDropMutationResponse, PostMessageMessageidDropPathParams } from "../types/PostMessageMessageidDrop";

 /**
     * @summary Drop a message
     * @link /message/:messageId/drop */
export async function postMessageMessageidDrop(messageId: PostMessageMessageidDropPathParams["messageId"], options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<PostMessageMessageidDropMutationResponse>> {
    const res = await client<PostMessageMessageidDropMutationResponse>({
        method: "post",
        url: `/message/${messageId}/drop`,
        ...options
    });
    return res;
}