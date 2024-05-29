package byzzbench.runner.controller;

import byzzbench.runner.Replica;
import byzzbench.runner.service.SimulatorService;
import byzzbench.runner.transport.Event;
import byzzbench.runner.transport.MessageEvent;
import byzzbench.runner.transport.MessageMutator;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Controller
public class SimulatorController {
    @Inject
    SimulatorService simulatorService;

    @Get(value = "/status", produces = MediaType.APPLICATION_JSON)
    public String getStatus() {
        return "Running";
    }

    @Get(value = "/nodes", produces = MediaType.APPLICATION_JSON)
    public List<String> getNodes() {
        return simulatorService.getScenarioExecutor().getNodes().keySet().stream().toList();
    }

    @Get(value = "/node/{nodeId}", produces = MediaType.APPLICATION_JSON)
    public Replica<? extends Serializable> getNode(String nodeId) {
        Replica<? extends Serializable> node = simulatorService.getScenarioExecutor().getNodes().get(nodeId);
        return node;
    }

    @Get(value = "/events", produces = MediaType.APPLICATION_JSON)
    public List<Long> getEvents() {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().keySet().stream().toList();
    }

    @Get(value = "/events/{eventId}", produces = MediaType.APPLICATION_JSON)
    public Event getEvent(Long eventId) {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().get(eventId);
    }

    @Get(value = "/messages/queued", produces = MediaType.APPLICATION_JSON)
    public List<Long> getQueuedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getMessagesInState(MessageEvent.MessageStatus.QUEUED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @Get(value = "/messages/dropped", produces = MediaType.APPLICATION_JSON)
    public List<Long> getDroppedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getMessagesInState(MessageEvent.MessageStatus.DROPPED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @Get(value = "/messages/delivered", produces = MediaType.APPLICATION_JSON)
    public List<Long> getDeliveredMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getMessagesInState(MessageEvent.MessageStatus.DELIVERED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @Get(value = "/message/{messageId}", produces = MediaType.APPLICATION_JSON)
    public Event getMessage(Long messageId) {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEvents().get(messageId);
    }

    @Post(value = "/message/{messageId}/deliver", produces = MediaType.APPLICATION_JSON)
    public void deliverMessage(Long messageId) throws Exception {
        simulatorService.getScenarioExecutor().getTransport().deliverMessage(messageId);
    }

    @Post(value = "/message/{messageId}/drop", produces = MediaType.APPLICATION_JSON)
    public void dropMessage(Long messageId) throws Exception {
        simulatorService.getScenarioExecutor().getTransport().dropMessage(messageId);
    }

    @Get(value = "/mutators", produces = MediaType.APPLICATION_JSON)
    public Set<Long> getMutators() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getMutators().keySet();
    }

    @Get(value = "/mutators/{mutatorId}", produces = MediaType.APPLICATION_JSON)
    public MutatorDTO getMutator(Long mutatorId) {
        MessageMutator m = simulatorService.getScenarioExecutor().getTransport()
                .getMutators().get(mutatorId);
        return new MutatorDTO(m);
    }

    @Post(value = "/reset", produces = MediaType.APPLICATION_JSON)
    public void reset() {
        simulatorService.getScenarioExecutor().reset();
    }

}
