import { z } from "zod";


/**
 * @description The status of the message delivery
 */
export const postDelivermessageMessageid200Schema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });

 /**
       * @description The status of the message delivery
       */
export const postDelivermessageMessageidMutationResponseSchema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });
export const postDelivermessageMessageidPathParamsSchema = z.object({ "messageId": z.string().describe("The ID of the message to deliver") });