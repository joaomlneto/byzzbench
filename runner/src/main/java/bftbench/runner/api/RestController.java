package bftbench.runner.api;

import bftbench.runner.ScenarioExecutor;
import bftbench.runner.transport.MessageEvent;
import bftbench.runner.transport.MessageMutator;
import spark.ResponseTransformer;

import java.io.Serializable;
import java.util.Map;

import static spark.Spark.*;

public class RestController {
    private static final ResponseTransformer jsonTransformer = new JsonResponseTransformer();

    private final ScenarioExecutor scenarioExecutor;

    public RestController(ScenarioExecutor scenarioExecutor) {
        this.scenarioExecutor = scenarioExecutor;
    }

    public void initialize() {
        setupCors();
        setupEndpoints();
        awaitInitialization();
    }

    public void setupCors() {
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
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
    }

    public void setupEndpoints() {
        get("/nodes", (req, res) -> scenarioExecutor.getNodes().keySet(), jsonTransformer);
        get("/node/:nodeId", (req, res) -> scenarioExecutor.getNodes().get(req.params(":nodeId")).getState(), jsonTransformer);
        get("/message/:messageId", (req, res) -> scenarioExecutor.getTransport().getMessages().get(Long.parseLong(req.params(":messageId"))), jsonTransformer);
        get("/messages/queued", (req, res) -> scenarioExecutor.getTransport().getMessagesInState(MessageEvent.MessageStatus.QUEUED).stream().map(MessageEvent::getMessageId).toArray(), jsonTransformer);
        get("/messages/dropped", (req, res) -> scenarioExecutor.getTransport().getMessagesInState(MessageEvent.MessageStatus.DROPPED).stream().map(MessageEvent::getMessageId).toArray(), jsonTransformer);
        get("/messages/delivered", (req, res) -> scenarioExecutor.getTransport().getMessagesInState(MessageEvent.MessageStatus.DELIVERED).stream().map(MessageEvent::getMessageId).toArray(), jsonTransformer);
        get("/mutators", (req, res) -> scenarioExecutor.getTransport().getMutators().keySet(), jsonTransformer);
        get("/mutators/:mutatorId", (req, res) -> scenarioExecutor.getTransport().getMutators().get(Long.parseLong(req.params(":mutatorId"))), jsonTransformer);
        get("/message/:messageId/mutators", (req, res) -> {
            Serializable message = scenarioExecutor.getTransport().getMessages().get(Long.parseLong(req.params(":messageId")));
            System.out.println("Message: " + message);
            return scenarioExecutor.getTransport().getMutators().entrySet().stream().filter(e -> e.getValue().getInputClasses().contains(message.getClass())).map(Map.Entry::getKey).toList();
        }, jsonTransformer);
        post("/message/:messageId/deliver", (req, res) -> {
            scenarioExecutor.getTransport().deliverMessage(Long.parseLong(req.params(":messageId")));
            return "OK";
        }, jsonTransformer);
        post("/message/:messageId/mutate/:mutatorId", (req, res) -> {
            Serializable message = scenarioExecutor.getTransport().getMessages().get(Long.parseLong(req.params(":messageId")));
            MessageMutator mutator = scenarioExecutor.getTransport().getMutators().get(Long.parseLong(req.params(":mutatorId")));
            if (!mutator.getInputClasses().contains(message.getClass())) {
                throw new RuntimeException("Mutator " + mutator.getName() + " does not support message type " + message.getClass().getName());
            }
            Serializable mutatedMessage = mutator.apply(message);
            System.out.println("Mutated message: " + message);
            return mutatedMessage;
        }, jsonTransformer);
        post("/message/:messageId/drop", (req, res) -> {
            scenarioExecutor.getTransport().dropMessage(Long.parseLong(req.params(":messageId")));
            return "OK";
        }, jsonTransformer);
        post("/reset", (req, res) -> {
            scenarioExecutor.reset();
            return "OK";
        }, jsonTransformer);
    }
}
