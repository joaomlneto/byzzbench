import { defineConfig } from "@kubb/core";
import createSwagger from "@kubb/swagger";
import createSwaggerClient from "@kubb/swagger-client";
import createSwaggerTanstackQuery from "@kubb/swagger-tanstack-query";
import createSwaggerTS from "@kubb/swagger-ts";
import createSwaggerZod from "@kubb/swagger-zod";

export default defineConfig(async () => {
  return {
    root: ".",
    input: {
      path: "http://localhost:8080/spec",
    },
    output: {
      path: "./lib/byzzbench-client/generated",
    },
    plugins: [
      createSwagger({ validate: true }),
      createSwaggerClient({
        client: {
          importPath: "../../client",
        },
        dataReturnType: "full",
      }),
      createSwaggerTS({}),
      createSwaggerTanstackQuery({
        framework: "react",
        client: {
          importPath: "../../client",
        },
        dataReturnType: "full",
      }),
      createSwaggerZod({}),
    ],
  };
});
