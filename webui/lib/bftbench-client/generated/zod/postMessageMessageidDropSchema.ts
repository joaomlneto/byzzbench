import { z } from "zod";


/**
 * @description The status of the message delivery
 */
export const postMessageMessageidDrop200Schema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });

 /**
       * @description The status of the message delivery
       */
export const postMessageMessageidDropMutationResponseSchema = z.object({ "status": z.string().describe("The status of the message delivery").optional() });
export const postMessageMessageidDropPathParamsSchema = z.object({ "messageId": z.string().describe("The ID of the message to drop") });