package byzzbench.simulator.controller;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.state.adob.AdobCache;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageMutator;
import java.io.Serializable;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for interacting with the simulator.
 */
@RestController
@RequiredArgsConstructor
public class SimulatorController {
  private final SimulatorService simulatorService;

  @GetMapping("/status")
  public String getStatus() {
    return "Running";
  }

  @GetMapping("/clients")
  public Set<String> getClients() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getClients()
        .keySet();
  }

  @GetMapping("/client/{clientId}")
  public Client<? extends Serializable>
  getClient(@PathVariable String clientId) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getClients()
        .get(clientId);
  }

  @GetMapping("/nodes")
  public Set<String> getNodes() {
    return simulatorService.getScenarioExecutor().getTransport().getNodeIds();
  }

  @GetMapping("/node/{nodeId}")
  public Replica<? extends Serializable> getNode(@PathVariable String nodeId) {
    return simulatorService.getScenarioExecutor().getTransport().getNode(
        nodeId);
  }

  @GetMapping("/node/{nodeId}/mailbox")
  public List<Long>
  getNodeMailbox(@PathVariable String nodeId,
                 @RequestParam(required = false) String type) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEventsInState(Event.Status.QUEUED)
        .stream()
        .filter(e -> type == null || e.getClass().getSimpleName().equals(type))
        .filter(e -> e.getRecipientId().equals(nodeId))
        .map(Event::getEventId)
        .toList();
  }

  @GetMapping("/events")
  public List<Long> getEvents() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEvents()
        .keySet()
        .stream()
        .toList();
  }

  @GetMapping("/events/{eventId}")
  public Event getEvent(@PathVariable Long eventId) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEvents()
        .get(eventId);
  }

  @GetMapping("/events/queued")
  public List<Long> getQueuedMessages() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEventsInState(Event.Status.QUEUED)
        .stream()
        .map(Event::getEventId)
        .toList();
  }

  @GetMapping("/events/dropped")
  public List<Long> getDroppedMessages() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEventsInState(Event.Status.DROPPED)
        .stream()
        .map(Event::getEventId)
        .toList();
  }

  @GetMapping("/events/delivered")
  public List<Long> getDeliveredMessages() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEventsInState(Event.Status.DELIVERED)
        .stream()
        .map(Event::getEventId)
        .toList();
  }

  @GetMapping("/schedule")
  public List<Long> getSchedule() {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getSchedule()
        .stream()
        .map(Event::getEventId)
        .toList();
  }

  @GetMapping("/event/{eventId}")
  public Event getMessage(@PathVariable Long eventId) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEvents()
        .get(eventId);
  }

  @GetMapping("/event/{eventId}/mutators")
  public List<Long> getMessageMutators(@PathVariable Long eventId) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getEventMutators(eventId)
        .stream()
        .map(Map.Entry::getKey)
        .toList();
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
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getMutators()
        .keySet();
  }

  @GetMapping("/mutators/{mutatorId}")
  public MessageMutator getMutator(@PathVariable Long mutatorId) {
    return simulatorService.getScenarioExecutor()
        .getTransport()
        .getMutators()
        .get(mutatorId);
  }

  @PostMapping("/reset")
  public void reset() throws Exception {
    simulatorService.getScenarioExecutor().reset();
  }

  @PostMapping("/scheduler/next")
  public Optional<Long> scheduleNext() throws Exception {
    Optional<Event> event =
        simulatorService.getScenarioExecutor().getScheduler().scheduleNext();
    return event.map(Event::getEventId);
  }

  @GetMapping("/adob")
  public AdobCache getAdob() {
    System.out.println("Getting AdoB!");
    return simulatorService.getScenarioExecutor().getAdobOracle().getRoot();
  }

  @GetMapping("/adob/caches")
  public Collection<AdobCache> getAllAdobCaches() {
    return simulatorService.getScenarioExecutor()
        .getAdobOracle()
        .getCaches()
        .values();
  }

  @GetMapping("/adob/caches/{cacheId}")
  public AdobCache getAdobCache(@PathVariable Long cacheId) {
    return simulatorService.getScenarioExecutor()
        .getAdobOracle()
        .getCaches()
        .get(cacheId);
  }
}
