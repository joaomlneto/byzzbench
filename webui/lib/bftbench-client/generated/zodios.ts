import { makeApi, Zodios } from "@zodios/core";
import { getNodesQueryResponseSchema } from "./zod/getNodesSchema";
import { getNodeByIdQueryResponseSchema, getNodeByIdPathParamsSchema } from "./zod/getNodeByIdSchema";
import { getCaptivemessagesQueryResponseSchema } from "./zod/getCaptivemessagesSchema";
import { getMessagesCaptiveQueryResponseSchema } from "./zod/getMessagesCaptiveSchema";
import { getMessagesDeliveredQueryResponseSchema } from "./zod/getMessagesDeliveredSchema";
import { getMessagesDroppedQueryResponseSchema } from "./zod/getMessagesDroppedSchema";
import { postMessageMessageidDeliverMutationResponseSchema, postMessageMessageidDeliverPathParamsSchema } from "./zod/postMessageMessageidDeliverSchema";
import { postMessageMessageidDropMutationResponseSchema, postMessageMessageidDropPathParamsSchema } from "./zod/postMessageMessageidDropSchema";
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
        path: "/node/:nodeId",
        description: ``,
        requestFormat: "json",
        parameters: [
            {
                name: "nodeId",
                description: `The ID of the node`,
                type: "Path",
                schema: getNodeByIdPathParamsSchema.shape["nodeId"]
            }
        ],
        response: getNodeByIdQueryResponseSchema,
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
        method: "get",
        path: "/messages/captive",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: getMessagesCaptiveQueryResponseSchema,
        errors: [],
    },
    {
        method: "get",
        path: "/messages/delivered",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: getMessagesDeliveredQueryResponseSchema,
        errors: [],
    },
    {
        method: "get",
        path: "/messages/dropped",
        description: ``,
        requestFormat: "json",
        parameters: [],
        response: getMessagesDroppedQueryResponseSchema,
        errors: [],
    },
    {
        method: "post",
        path: "/message/:messageId/deliver",
        description: ``,
        requestFormat: "json",
        parameters: [
            {
                name: "messageId",
                description: `The ID of the message to deliver`,
                type: "Path",
                schema: postMessageMessageidDeliverPathParamsSchema.shape["messageId"]
            }
        ],
        response: postMessageMessageidDeliverMutationResponseSchema,
        errors: [],
    },
    {
        method: "post",
        path: "/message/:messageId/drop",
        description: ``,
        requestFormat: "json",
        parameters: [
            {
                name: "messageId",
                description: `The ID of the message to drop`,
                type: "Path",
                schema: postMessageMessageidDropPathParamsSchema.shape["messageId"]
            }
        ],
        response: postMessageMessageidDropMutationResponseSchema,
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