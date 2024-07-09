package byzzbench.simulator.controller;

import byzzbench.simulator.Replica;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessageMutator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<Long> getNodeMailbox(@PathVariable String nodeId, @RequestParam(required = false) String type) {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(e -> type == null || e.getClass().getSimpleName().equals(type))
                .filter(e -> e.getRecipientId().equals(nodeId))
                .map(Event::getEventId)
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

    @GetMapping("/events/queued")
    public List<Long> getQueuedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    @GetMapping("/events/dropped")
    public List<Long> getDroppedMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.DROPPED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    @GetMapping("/events/delivered")
    public List<Long> getDeliveredMessages() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getEventsInState(Event.Status.DELIVERED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    @GetMapping("/schedule")
    public List<Long> getSchedule() {
        return simulatorService.getScenarioExecutor().getTransport()
                .getSchedule()
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    @GetMapping("/event/{eventId}")
    public Event getMessage(@PathVariable Long eventId) {
        return simulatorService.getScenarioExecutor().getTransport().getEvents().get(eventId);
    }

    @GetMapping("/event/{eventId}/mutators")
    public Set<Long> getMessageMutators(@PathVariable Long eventId) {
        Event e = simulatorService.getScenarioExecutor().getTransport().getEvents().get(eventId);

        // if it is not a message, return an empty set
        if (!(e instanceof MessageEvent)) {
            return Set.of();
        }

        Set<Map.Entry<Long, MessageMutator>> messageMutators = simulatorService.getScenarioExecutor().getTransport()
                .getMutators().entrySet()
                .stream()
                // filter mutators that can be applied to the message
                .filter(entry -> entry.getValue().getInputClasses().contains(((MessageEvent) e).getPayload().getClass()))
                //.map(entry -> entry.getKey())
                .collect(Collectors.toSet());

        System.out.println("Message Mutators for " + e + ": " + messageMutators);

        // return their keys
        return messageMutators.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    @PostMapping("/event/{eventId}/deliver")
    public void deliverMessage(@PathVariable Long eventId) throws Exception {
        simulatorService.getScenarioExecutor().getTransport().deliverEvent(eventId);
    }

    @PostMapping("/event/{eventId}/drop")
    public void dropMessage(@PathVariable Long eventId) {
        simulatorService.getScenarioExecutor().getTransport().dropMessage(eventId);
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
    public void reset() throws Exception {
        simulatorService.getScenarioExecutor().reset();
    }

    @PostMapping("/scheduler/next")
    public Optional<Long> scheduleNext() throws Exception {
        Optional<Event> event = simulatorService.getScenarioExecutor().getScheduler().scheduleNext();
        return event.map(Event::getEventId);
    }


}
