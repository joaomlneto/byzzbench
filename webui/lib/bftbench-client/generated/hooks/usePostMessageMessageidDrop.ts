import client from "../../client";
import { useMutation } from "@tanstack/react-query";
import type { PostMessageMessageidDropMutationResponse, PostMessageMessageidDropPathParams } from "../types/PostMessageMessageidDrop";
import type { UseMutationOptions } from "@tanstack/react-query";

 type PostMessageMessageidDropClient = typeof client<PostMessageMessageidDropMutationResponse, never, never>;
type PostMessageMessageidDrop = {
    data: PostMessageMessageidDropMutationResponse;
    error: never;
    request: never;
    pathParams: PostMessageMessageidDropPathParams;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<PostMessageMessageidDropClient>>;
    client: {
        parameters: Partial<Parameters<PostMessageMessageidDropClient>[0]>;
        return: Awaited<ReturnType<PostMessageMessageidDropClient>>;
    };
};
/**
     * @summary Drop a message
     * @link /message/:messageId/drop */
export function usePostMessageMessageidDrop(messageId: PostMessageMessageidDropPathParams["messageId"], options: {
    mutation?: UseMutationOptions<PostMessageMessageidDrop["response"], PostMessageMessageidDrop["error"], PostMessageMessageidDrop["request"]>;
    client?: PostMessageMessageidDrop["client"]["parameters"];
} = {}) {
    const { mutation: mutationOptions, client: clientOptions = {} } = options ?? {};
    return useMutation({
        mutationFn: async () => {
            const res = await client<PostMessageMessageidDrop["data"], PostMessageMessageidDrop["error"], PostMessageMessageidDrop["request"]>({
                method: "post",
                url: `/message/${messageId}/drop`,
                ...clientOptions
            });
            return res;
        },
        ...mutationOptions
    });
}