import { z } from "zod";


/**
 * @description The list of dropped messages
 */
export const getMessagesDelivered200Schema = z.array(z.object({ "id": z.string().describe("The ID of the captive message").optional(), "status": z.string().describe("The status of the captive message").optional() }));

 /**
       * @description The list of dropped messages
       */
export const getMessagesDeliveredQueryResponseSchema = z.array(z.object({ "id": z.string().describe("The ID of the captive message").optional(), "status": z.string().describe("The status of the captive message").optional() }));