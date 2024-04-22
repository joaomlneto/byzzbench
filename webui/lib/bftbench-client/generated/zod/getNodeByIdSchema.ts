import { z } from "zod";


/**
 * @description OK
 */
export const getNodeById200Schema = z.object({}).catchall(z.object({}));
export const getNodeByIdPathParamsSchema = z.object({ "nodeId": z.string().describe("The ID of the node") });

 /**
       * @description OK
       */
export const getNodeByIdQueryResponseSchema = z.object({}).catchall(z.object({}));