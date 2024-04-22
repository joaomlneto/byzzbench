import { z } from "zod";


/**
 * @description The status of the message delivery
 */
export const postMessageMessageidDeliver200Schema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });

 /**
       * @description The status of the message delivery
       */
export const postMessageMessageidDeliverMutationResponseSchema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });
export const postMessageMessageidDeliverPathParamsSchema = z.object({ "messageId": z.string().describe("The ID of the message to deliver") });