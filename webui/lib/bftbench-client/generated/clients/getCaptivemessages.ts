import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetCaptivemessagesQueryResponse } from "../types/GetCaptivemessages";

 /**
     * @summary Get the list of captive messages
     * @link /captiveMessages */
export async function getCaptivemessages(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetCaptivemessagesQueryResponse>> {
    const res = await client<GetCaptivemessagesQueryResponse>({
        method: "get",
        url: `/captiveMessages`,
        ...options
    });
    return res;
}