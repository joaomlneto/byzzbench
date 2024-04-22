import { z } from "zod";


/**
 * @description OK
 */
export const getNodes200Schema = z.array(z.string());

 /**
       * @description OK
       */
export const getNodesQueryResponseSchema = z.array(z.string());