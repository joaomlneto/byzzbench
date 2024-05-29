import { defineConfig } from "@kubb/core";
import createSwagger from "@kubb/swagger";
import createSwaggerClient from "@kubb/swagger-client";
import createSwaggerTanstackQuery from "@kubb/swagger-tanstack-query";
import createSwaggerTS from "@kubb/swagger-ts";
import createSwaggerZod from "@kubb/swagger-zod";
import createSwaggerZodios from "@kubb/swagger-zodios";

/*
const specJson = fetch(
  "http://localhost:8080/swagger/byzzbench-api-0.1.yml",
).then((res) => res.json()); */

export default defineConfig(async () => {
  return {
    root: ".",
    input: {
      path: "http://localhost:8080/swagger/byzzbench-api-0.1.yml",
      //data: specJson,
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
      createSwaggerZodios({}),
    ],
  };
});
