import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetMessagesDroppedQueryResponse } from "../types/GetMessagesDropped";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetMessagesDroppedClient = typeof client<GetMessagesDroppedQueryResponse, never, never>;
type GetMessagesDropped = {
    data: GetMessagesDroppedQueryResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetMessagesDroppedClient>>;
    client: {
        parameters: Partial<Parameters<GetMessagesDroppedClient>[0]>;
        return: Awaited<ReturnType<GetMessagesDroppedClient>>;
    };
};
export const getMessagesDroppedQueryKey = () => [{ url: "/messages/dropped" }] as const;
export type GetMessagesDroppedQueryKey = ReturnType<typeof getMessagesDroppedQueryKey>;
export function getMessagesDroppedQueryOptions(options: GetMessagesDropped["client"]["parameters"] = {}) {
    const queryKey = getMessagesDroppedQueryKey();
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetMessagesDropped["data"], GetMessagesDropped["error"]>({
                method: "get",
                url: `/messages/dropped`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get the list of dropped messages
     * @link /messages/dropped */
export function useGetMessagesDropped<TData = GetMessagesDropped["response"], TQueryData = GetMessagesDropped["response"], TQueryKey extends QueryKey = GetMessagesDroppedQueryKey>(options: {
    query?: Partial<QueryObserverOptions<GetMessagesDropped["response"], GetMessagesDropped["error"], TData, TQueryData, TQueryKey>>;
    client?: GetMessagesDropped["client"]["parameters"];
} = {}): UseQueryResult<TData, GetMessagesDropped["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getMessagesDroppedQueryKey();
    const query = useQuery({
        ...getMessagesDroppedQueryOptions(clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetMessagesDropped["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}