import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetMessagesCaptiveQueryResponse } from "../types/GetMessagesCaptive";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetMessagesCaptiveClient = typeof client<GetMessagesCaptiveQueryResponse, never, never>;
type GetMessagesCaptive = {
    data: GetMessagesCaptiveQueryResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetMessagesCaptiveClient>>;
    client: {
        parameters: Partial<Parameters<GetMessagesCaptiveClient>[0]>;
        return: Awaited<ReturnType<GetMessagesCaptiveClient>>;
    };
};
export const getMessagesCaptiveQueryKey = () => [{ url: "/messages/captive" }] as const;
export type GetMessagesCaptiveQueryKey = ReturnType<typeof getMessagesCaptiveQueryKey>;
export function getMessagesCaptiveQueryOptions(options: GetMessagesCaptive["client"]["parameters"] = {}) {
    const queryKey = getMessagesCaptiveQueryKey();
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetMessagesCaptive["data"], GetMessagesCaptive["error"]>({
                method: "get",
                url: `/messages/captive`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get the list of captive messages
     * @link /messages/captive */
export function useGetMessagesCaptive<TData = GetMessagesCaptive["response"], TQueryData = GetMessagesCaptive["response"], TQueryKey extends QueryKey = GetMessagesCaptiveQueryKey>(options: {
    query?: Partial<QueryObserverOptions<GetMessagesCaptive["response"], GetMessagesCaptive["error"], TData, TQueryData, TQueryKey>>;
    client?: GetMessagesCaptive["client"]["parameters"];
} = {}): UseQueryResult<TData, GetMessagesCaptive["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getMessagesCaptiveQueryKey();
    const query = useQuery({
        ...getMessagesCaptiveQueryOptions(clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetMessagesCaptive["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}