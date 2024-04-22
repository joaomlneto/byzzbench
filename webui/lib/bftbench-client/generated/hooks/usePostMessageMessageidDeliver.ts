import client from "../../client";
import { useMutation } from "@tanstack/react-query";
import type { PostMessageMessageidDeliverMutationResponse, PostMessageMessageidDeliverPathParams } from "../types/PostMessageMessageidDeliver";
import type { UseMutationOptions } from "@tanstack/react-query";

 type PostMessageMessageidDeliverClient = typeof client<PostMessageMessageidDeliverMutationResponse, never, never>;
type PostMessageMessageidDeliver = {
    data: PostMessageMessageidDeliverMutationResponse;
    error: never;
    request: never;
    pathParams: PostMessageMessageidDeliverPathParams;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<PostMessageMessageidDeliverClient>>;
    client: {
        parameters: Partial<Parameters<PostMessageMessageidDeliverClient>[0]>;
        return: Awaited<ReturnType<PostMessageMessageidDeliverClient>>;
    };
};
/**
     * @summary Deliver a message
     * @link /message/:messageId/deliver */
export function usePostMessageMessageidDeliver(messageId: PostMessageMessageidDeliverPathParams["messageId"], options: {
    mutation?: UseMutationOptions<PostMessageMessageidDeliver["response"], PostMessageMessageidDeliver["error"], PostMessageMessageidDeliver["request"]>;
    client?: PostMessageMessageidDeliver["client"]["parameters"];
} = {}) {
    const { mutation: mutationOptions, client: clientOptions = {} } = options ?? {};
    return useMutation({
        mutationFn: async () => {
            const res = await client<PostMessageMessageidDeliver["data"], PostMessageMessageidDeliver["error"], PostMessageMessageidDeliver["request"]>({
                method: "post",
                url: `/message/${messageId}/deliver`,
                ...clientOptions
            });
            return res;
        },
        ...mutationOptions
    });
}