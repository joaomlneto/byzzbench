import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetMessagesDroppedQueryResponse } from "../types/GetMessagesDropped";

 /**
     * @summary Get the list of dropped messages
     * @link /messages/dropped */
export async function getMessagesDropped(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetMessagesDroppedQueryResponse>> {
    const res = await client<GetMessagesDroppedQueryResponse>({
        method: "get",
        url: `/messages/dropped`,
        ...options
    });
    return res;
}