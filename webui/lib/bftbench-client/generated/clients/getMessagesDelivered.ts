import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetMessagesDeliveredQueryResponse } from "../types/GetMessagesDelivered";

 /**
     * @summary Get the list of dropped messages
     * @link /messages/delivered */
export async function getMessagesDelivered(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetMessagesDeliveredQueryResponse>> {
    const res = await client<GetMessagesDeliveredQueryResponse>({
        method: "get",
        url: `/messages/delivered`,
        ...options
    });
    return res;
}