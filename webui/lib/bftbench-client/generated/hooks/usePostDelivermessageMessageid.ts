import client from "../../client";
import { useMutation } from "@tanstack/react-query";
import type { PostDelivermessageMessageidMutationResponse, PostDelivermessageMessageidPathParams } from "../types/PostDelivermessageMessageid";
import type { UseMutationOptions } from "@tanstack/react-query";

 type PostDelivermessageMessageidClient = typeof client<PostDelivermessageMessageidMutationResponse, never, never>;
type PostDelivermessageMessageid = {
    data: PostDelivermessageMessageidMutationResponse;
    error: never;
    request: never;
    pathParams: PostDelivermessageMessageidPathParams;
    queryParams: never;
    headerParams: never;
    response: Awaited<ReturnType<PostDelivermessageMessageidClient>>;
    client: {
        parameters: Partial<Parameters<PostDelivermessageMessageidClient>[0]>;
        return: Awaited<ReturnType<PostDelivermessageMessageidClient>>;
    };
};
/**
     * @summary Deliver a message
     * @link /deliverMessage/:messageId */
export function usePostDelivermessageMessageid(messageId: PostDelivermessageMessageidPathParams["messageId"], options: {
    mutation?: UseMutationOptions<PostDelivermessageMessageid["response"], PostDelivermessageMessageid["error"], PostDelivermessageMessageid["request"]>;
    client?: PostDelivermessageMessageid["client"]["parameters"];
} = {}) {
    const { mutation: mutationOptions, client: clientOptions = {} } = options ?? {};
    return useMutation({
        mutationFn: async () => {
            const res = await client<PostDelivermessageMessageid["data"], PostDelivermessageMessageid["error"], PostDelivermessageMessageid["request"]>({
                method: "post",
                url: `/deliverMessage/${messageId}`,
                ...clientOptions
            });
            return res;
        },
        ...mutationOptions
    });
}