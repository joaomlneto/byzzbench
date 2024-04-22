package bftbench.runner;

import bftbench.runner.api.JsonResponseTransformer;
import bftbench.runner.pbft.PbftScenarioExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import spark.ResponseTransformer;

import java.io.IOException;
import java.io.StringWriter;

import static spark.Spark.*;

public class App {
    private static final ResponseTransformer jsonTransformer = new JsonResponseTransformer();

    @Data
    public static class StringResponse {
        private final String message;
    }

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
    public static void main(String[] args) {
        ScenarioExecutor r = new PbftScenarioExecutor();

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

        get("/nodes", (req, res) -> r.getNodes().keySet(), jsonTransformer);
        get("/node/:nodeId", (req, res) -> r.getNodes().get(req.params(":nodeId")), jsonTransformer);
        get("/message/:messageId", (req, res) -> r.getTransport().getMessages().get(Long.parseLong(req.params(":messageId"))), jsonTransformer);
        get("/messages/captive", (req, res) -> r.getTransport().getQueuedMessages(), jsonTransformer);
        get("/messages/dropped", (req, res) -> r.getTransport().getDroppedMessages(), jsonTransformer);
        get("/messages/delivered", (req, res) -> r.getTransport().getDeliveredMessages(), jsonTransformer);
        post("/message/:messageId/deliver", (req, res) -> { r.getTransport().deliverMessage(Long.parseLong(req.params(":messageId"))); return new StringResponse("OK"); }, jsonTransformer);
        post("/message/:messageId/drop", (req, res) -> { r.getTransport().dropMessage(Long.parseLong(req.params(":messageId"))); return new StringResponse("OK"); }, jsonTransformer);
        post("/reset", (req, res) -> {r.reset(); return new StringResponse("OK"); }, jsonTransformer);

        awaitInitialization();
        r.run();
    }
}
