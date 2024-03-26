package bftbench.runner;

import bftbench.runner.api.JsonResponseTransformer;
import lombok.Data;

import static spark.Spark.*;

public class App {
    @Data
    public static class StringResponse {
        private final String message;
    }
    public static void main(String[] args) {
        PbftRunner r = new PbftRunner(4);

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        get("/", (req, res) -> "Hello World", new JsonResponseTransformer());
        get("/nodes", (req, res) -> r.nodes(), new JsonResponseTransformer());
        get("/captiveMessages", (req, res) -> r.captiveMessages(), new JsonResponseTransformer());
        post("/deliverMessage/:messageId", (req, res) -> {
            r.deliverMessage(Long.parseLong(req.params(":messageId")));
            return new StringResponse("OK");
        }, new JsonResponseTransformer());
        post("/reset", (req, res) -> {
            r.reset();
            return new StringResponse("OK");
        }, new JsonResponseTransformer());

        awaitInitialization();
        r.run();
    }
}
