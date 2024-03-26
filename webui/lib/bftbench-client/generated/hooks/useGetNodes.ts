import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetNodesQueryResponse } from "../types/GetNodes";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetNodesClient = typeof client<GetNodesQueryResponse, never, never>;
type GetNodes = {
    data: GetNodesQueryResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetNodesClient>>;
    client: {
        parameters: Partial<Parameters<GetNodesClient>[0]>;
        return: Awaited<ReturnType<GetNodesClient>>;
    };
};
export const getNodesQueryKey = () => [{ url: "/nodes" }] as const;
export type GetNodesQueryKey = ReturnType<typeof getNodesQueryKey>;
export function getNodesQueryOptions(options: GetNodes["client"]["parameters"] = {}) {
    const queryKey = getNodesQueryKey();
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetNodes["data"], GetNodes["error"]>({
                method: "get",
                url: `/nodes`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get the list of nodes
     * @link /nodes */
export function useGetNodes<TData = GetNodes["response"], TQueryData = GetNodes["response"], TQueryKey extends QueryKey = GetNodesQueryKey>(options: {
    query?: Partial<QueryObserverOptions<GetNodes["response"], GetNodes["error"], TData, TQueryData, TQueryKey>>;
    client?: GetNodes["client"]["parameters"];
} = {}): UseQueryResult<TData, GetNodes["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getNodesQueryKey();
    const query = useQuery({
        ...getNodesQueryOptions(clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetNodes["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}