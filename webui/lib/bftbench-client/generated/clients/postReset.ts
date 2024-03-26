import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { PostResetMutationResponse } from "../types/PostReset";

 /**
     * @summary Reset the BFT Bench
     * @link /reset */
export async function postReset(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<PostResetMutationResponse>> {
    const res = await client<PostResetMutationResponse>({
        method: "post",
        url: `/reset`,
        ...options
    });
    return res;
}