import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetMessagesDeliveredQueryResponse } from "../types/GetMessagesDelivered";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetMessagesDeliveredClient = typeof client<GetMessagesDeliveredQueryResponse, never, never>;
type GetMessagesDelivered = {
    data: GetMessagesDeliveredQueryResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetMessagesDeliveredClient>>;
    client: {
        parameters: Partial<Parameters<GetMessagesDeliveredClient>[0]>;
        return: Awaited<ReturnType<GetMessagesDeliveredClient>>;
    };
};
export const getMessagesDeliveredQueryKey = () => [{ url: "/messages/delivered" }] as const;
export type GetMessagesDeliveredQueryKey = ReturnType<typeof getMessagesDeliveredQueryKey>;
export function getMessagesDeliveredQueryOptions(options: GetMessagesDelivered["client"]["parameters"] = {}) {
    const queryKey = getMessagesDeliveredQueryKey();
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetMessagesDelivered["data"], GetMessagesDelivered["error"]>({
                method: "get",
                url: `/messages/delivered`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get the list of dropped messages
     * @link /messages/delivered */
export function useGetMessagesDelivered<TData = GetMessagesDelivered["response"], TQueryData = GetMessagesDelivered["response"], TQueryKey extends QueryKey = GetMessagesDeliveredQueryKey>(options: {
    query?: Partial<QueryObserverOptions<GetMessagesDelivered["response"], GetMessagesDelivered["error"], TData, TQueryData, TQueryKey>>;
    client?: GetMessagesDelivered["client"]["parameters"];
} = {}): UseQueryResult<TData, GetMessagesDelivered["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getMessagesDeliveredQueryKey();
    const query = useQuery({
        ...getMessagesDeliveredQueryOptions(clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetMessagesDelivered["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}