import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetNodesQueryResponse } from "../types/GetNodes";

 /**
     * @summary Get the list of nodes
     * @link /nodes */
export async function getNodes(options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetNodesQueryResponse>> {
    const res = await client<GetNodesQueryResponse>({
        method: "get",
        url: `/nodes`,
        ...options
    });
    return res;
}