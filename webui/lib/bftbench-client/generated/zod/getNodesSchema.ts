import { z } from "zod";


/**
 * @description OK
 */
export const getNodes200Schema = z.object({}).catchall(z.object({}));

 /**
       * @description OK
       */
export const getNodesQueryResponseSchema = z.object({}).catchall(z.object({}));