import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetNodeByIdQueryResponse, GetNodeByIdPathParams } from "../types/GetNodeById";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetNodeByIdClient = typeof client<GetNodeByIdQueryResponse, never, never>;
type GetNodeById = {
    data: GetNodeByIdQueryResponse;
    error: never;
    request: never;
    pathParams: GetNodeByIdPathParams;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetNodeByIdClient>>;
    client: {
        parameters: Partial<Parameters<GetNodeByIdClient>[0]>;
        return: Awaited<ReturnType<GetNodeByIdClient>>;
    };
};
export const getNodeByIdQueryKey = (nodeId: GetNodeByIdPathParams["nodeId"]) => [{ url: "/node/:nodeId", params: { nodeId: nodeId } }] as const;
export type GetNodeByIdQueryKey = ReturnType<typeof getNodeByIdQueryKey>;
export function getNodeByIdQueryOptions(nodeId: GetNodeByIdPathParams["nodeId"], options: GetNodeById["client"]["parameters"] = {}) {
    const queryKey = getNodeByIdQueryKey(nodeId);
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetNodeById["data"], GetNodeById["error"]>({
                method: "get",
                url: `/node/${nodeId}`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get a Node by its ID
     * @link /node/:nodeId */
export function useGetNodeById<TData = GetNodeById["response"], TQueryData = GetNodeById["response"], TQueryKey extends QueryKey = GetNodeByIdQueryKey>(nodeId: GetNodeByIdPathParams["nodeId"], options: {
    query?: Partial<QueryObserverOptions<GetNodeById["response"], GetNodeById["error"], TData, TQueryData, TQueryKey>>;
    client?: GetNodeById["client"]["parameters"];
} = {}): UseQueryResult<TData, GetNodeById["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getNodeByIdQueryKey(nodeId);
    const query = useQuery({
        ...getNodeByIdQueryOptions(nodeId, clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetNodeById["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}