import client from "../../client";
import { useQuery, queryOptions } from "@tanstack/react-query";
import type { GetCaptivemessagesQueryResponse } from "../types/GetCaptivemessages";
import type { QueryObserverOptions, UseQueryResult, QueryKey } from "@tanstack/react-query";

 type GetCaptivemessagesClient = typeof client<GetCaptivemessagesQueryResponse, never, never>;
type GetCaptivemessages = {
    data: GetCaptivemessagesQueryResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<GetCaptivemessagesClient>>;
    client: {
        parameters: Partial<Parameters<GetCaptivemessagesClient>[0]>;
        return: Awaited<ReturnType<GetCaptivemessagesClient>>;
    };
};
export const getCaptivemessagesQueryKey = () => [{ url: "/captiveMessages" }] as const;
export type GetCaptivemessagesQueryKey = ReturnType<typeof getCaptivemessagesQueryKey>;
export function getCaptivemessagesQueryOptions(options: GetCaptivemessages["client"]["parameters"] = {}) {
    const queryKey = getCaptivemessagesQueryKey();
    return queryOptions({
        queryKey,
        queryFn: async () => {
            const res = await client<GetCaptivemessages["data"], GetCaptivemessages["error"]>({
                method: "get",
                url: `/captiveMessages`,
                ...options
            });
            return res;
        },
    });
}
/**
     * @summary Get the list of captive messages
     * @link /captiveMessages */
export function useGetCaptivemessages<TData = GetCaptivemessages["response"], TQueryData = GetCaptivemessages["response"], TQueryKey extends QueryKey = GetCaptivemessagesQueryKey>(options: {
    query?: Partial<QueryObserverOptions<GetCaptivemessages["response"], GetCaptivemessages["error"], TData, TQueryData, TQueryKey>>;
    client?: GetCaptivemessages["client"]["parameters"];
} = {}): UseQueryResult<TData, GetCaptivemessages["error"]> & {
    queryKey: TQueryKey;
} {
    const { query: queryOptions, client: clientOptions = {} } = options ?? {};
    const queryKey = queryOptions?.queryKey ?? getCaptivemessagesQueryKey();
    const query = useQuery({
        ...getCaptivemessagesQueryOptions(clientOptions) as QueryObserverOptions,
        queryKey,
        ...queryOptions as unknown as Omit<QueryObserverOptions, "queryKey">
    }) as UseQueryResult<TData, GetCaptivemessages["error"]> & {
        queryKey: TQueryKey;
    };
    query.queryKey = queryKey as TQueryKey;
    return query;
}