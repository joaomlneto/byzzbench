import client from "../../client";
import { useMutation } from "@tanstack/react-query";
import type { PostResetMutationResponse } from "../types/PostReset";
import type { UseMutationOptions } from "@tanstack/react-query";

 type PostResetClient = typeof client<PostResetMutationResponse, never, never>;
type PostReset = {
    data: PostResetMutationResponse;
    error: never;
    request: never;
    pathParams: never;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<PostResetClient>>;
    client: {
        parameters: Partial<Parameters<PostResetClient>[0]>;
        return: Awaited<ReturnType<PostResetClient>>;
    };
};
/**
     * @summary Reset the BFT Bench
     * @link /reset */
export function usePostReset(options: {
    mutation?: UseMutationOptions<PostReset["response"], PostReset["error"], PostReset["request"]>;
    client?: PostReset["client"]["parameters"];
} = {}) {
    const { mutation: mutationOptions, client: clientOptions = {} } = options ?? {};
    return useMutation({
        mutationFn: async () => {
            const res = await client<PostReset["data"], PostReset["error"], PostReset["request"]>({
                method: "post",
                url: `/reset`,
                ...clientOptions
            });
            return res;
        },
        ...mutationOptions
    });
}