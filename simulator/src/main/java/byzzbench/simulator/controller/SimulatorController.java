package byzzbench.simulator.controller;

import byzzbench.simulator.Replica;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessageMutator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class SimulatorController {
    private final SimulatorService simulatorService;

    @GetMapping("/status")
    public String getStatus() {
        return "Running";
    }

    @GetMapping("/nodes")
    public List<String> getNodes() {
        return simulatorService.getScenarioExecutor().getNodes().keySet().stream().toList();
    }

    @GetMapping("/node/{nodeId}")
    public Replica<? extends Serializable> getNode(@PathVariable String nodeId) {
        return simulatorService.getScenarioExecutor().getNodes().get(nodeId);
    }

    @GetMapping("/node/{nodeId}/mailbox")
    public List<Long> getNodeMailbox(@PathVariable String nodeId) {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(e -> e.getRecipientId().equals(nodeId))
                .map(MessageEvent::getEventId)
                .toList();
    }

    @GetMapping("/events")
    public List<Long> getEvents() {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().keySet().stream().toList();
    }

    @GetMapping("/events/{eventId}")
    public Event getEvent(@PathVariable Long eventId) {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().get(eventId);
    }

    @GetMapping("/messages/queued")
    public List<Long> getQueuedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @GetMapping("/messages/dropped")
    public List<Long> getDroppedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.DROPPED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @GetMapping("/messages/delivered")
    public List<Long> getDeliveredMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.DELIVERED)
                .stream()
                .map(MessageEvent::getEventId)
                .toList();
    }

    @GetMapping("/message/{messageId}")
    public Event getMessage(@PathVariable Long messageId) {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().get(messageId);
    }

    @PostMapping("/message/{messageId}/deliver")
    public void deliverMessage(@PathVariable Long messageId) throws Exception {
        simulatorService.getScenarioExecutor().getTransport().deliverEvent(messageId);
    }

    @PostMapping("/message/{messageId}/drop")
    public void dropMessage(@PathVariable Long messageId) {
        simulatorService.getScenarioExecutor().getTransport().dropMessage(messageId);
    }

    @GetMapping("/mutators")
    public Set<Long> getMutators() {
        return simulatorService.getScenarioExecutor().getTransport().getMutators().keySet();
    }

    @GetMapping("/mutators/{mutatorId}")
    public MessageMutator getMutator(@PathVariable Long mutatorId) {
        return simulatorService.getScenarioExecutor().getTransport().getMutators().get(mutatorId);
    }

    @PostMapping("/reset")
    public void reset() {
        simulatorService.getScenarioExecutor().reset();
    }


}
