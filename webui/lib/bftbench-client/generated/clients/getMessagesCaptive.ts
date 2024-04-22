import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetMessagesCaptiveQueryResponse } from "../types/GetMessagesCaptive";

 /**
     * @summary Get the list of captive messages
     * @link /messages/captive */
export async function getMessagesCaptive(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetMessagesCaptiveQueryResponse>> {
    const res = await client<GetMessagesCaptiveQueryResponse>({
        method: "get",
        url: `/messages/captive`,
        ...options
    });
    return res;
}