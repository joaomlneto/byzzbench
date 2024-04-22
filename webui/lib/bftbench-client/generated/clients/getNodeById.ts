import client from "../../client";
import type { ResponseConfig } from "../../client";
import type { GetNodeByIdQueryResponse, GetNodeByIdPathParams } from "../types/GetNodeById";

 /**
     * @summary Get a Node by its ID
     * @link /node/:nodeId */
export async function getNodeById(nodeId: GetNodeByIdPathParams["nodeId"], options: Partial<Parameters<typeof client>[0]> = {}): Promise<ResponseConfig<GetNodeByIdQueryResponse>> {
    const res = await client<GetNodeByIdQueryResponse>({
        method: "get",
        url: `/node/${nodeId}`,
        ...options
    });
    return res;
}