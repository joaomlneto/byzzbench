import { z } from "zod";


/**
 * @description The list of captive messages
 */
export const getMessagesCaptive200Schema = z.array(z.object({ "id": z.string().describe("The ID of the captive message").optional(), "status": z.string().describe("The status of the captive message").optional() }));

 /**
       * @description The list of captive messages
       */
export const getMessagesCaptiveQueryResponseSchema = z.array(z.object({ "id": z.string().describe("The ID of the captive message").optional(), "status": z.string().describe("The status of the captive message").optional() }));