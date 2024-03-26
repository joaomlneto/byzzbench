import { makeApi, Zodios } from "@zodios/core";
import { getNodesQueryResponseSchema } from "./zod/getNodesSchema";
import { getCaptivemessagesQueryResponseSchema } from "./zod/getCaptivemessagesSchema";
import { postDelivermessageMessageidMutationResponseSchema, postDelivermessageMessageidPathParamsSchema } from "./zod/postDelivermessageMessageidSchema";
import { postResetMutationResponseSchema } from "./zod/postResetSchema";

 export const endpoints = makeApi([
    {
        method: "get",
        path: "/nodes",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: getNodesQueryResponseSchema,
        errors: [],
    },
    {
        method: "get",
        path: "/captiveMessages",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: getCaptivemessagesQueryResponseSchema,
        errors: [],
    },
    {
        method: "post",
        path: "/deliverMessage/:messageId",
        description: ``,
        requestFormat: "json",
        parameters: [
            {
                name: "messageId",
                description: `The ID of the message to deliver`,
                type: "Path",
                schema: postDelivermessageMessageidPathParamsSchema.shape["messageId"]
            }
        ],
        response: postDelivermessageMessageidMutationResponseSchema,
        errors: [],
    },
    {
        method: "post",
        path: "/reset",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: postResetMutationResponseSchema,
        errors: [],
    }
]);
export const getAPI = (baseUrl: string) => new Zodios(baseUrl, endpoints);
export const api = new Zodios("http://localhost:4567", endpoints);
export default api;