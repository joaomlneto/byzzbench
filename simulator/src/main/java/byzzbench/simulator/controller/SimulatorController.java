package byzzbench.simulator.controller;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.service.ScenarioFactoryService;
import byzzbench.simulator.service.SchedulerFactoryService;
import byzzbench.simulator.service.SchedulesService;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.state.adob.AdobCache;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST API for interacting with the simulator.
 */
@RestController
@RequiredArgsConstructor
public class SimulatorController {
    private final SchedulesService schedulesService;
    private final SimulatorService simulatorService;
    private final ScenarioFactoryService scenarioFactoryService;
    private final SchedulerFactoryService schedulerFactoryService;

    /**
     * Get the status of the simulator.
     * @return The status of the simulator.
     */
    @GetMapping("/status")
    public String getStatus() {
        return "Running";
    }

    /**
     * Get the list of available client IDs in the current scenario.
     * @return The list of client IDs.
     */
    @GetMapping("/clients")
    public Set<String> getClients() {
        return simulatorService.getScenarioExecutor()
                .getTransport()
                .getClients()
                .keySet();
    }

    /**
     * Get the client with the given ID.
     * @param clientId The ID of the client to get.
     * @return The client with the given ID.
     */
    @GetMapping("/client/{clientId}")
    public Client<? extends Serializable> getClient(@PathVariable String clientId) {
        return simulatorService.getScenarioExecutor().getTransport().getClients().get(clientId);
    }

    @GetMapping("/nodes")
    public Set<String> getNodes() {
        return simulatorService.getScenarioExecutor()
                .getTransport()
                .getNodeIds();
    }

    @GetMapping("/node/{nodeId}")
    public Replica<? extends Serializable> getNode(@PathVariable String nodeId) {
        return simulatorService.getScenarioExecutor().getTransport().getNode(nodeId);
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

    /**
     * Get the current schedule of the simulator.
     * @return The list of delivered event IDs in order.
     */
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

    /**
     * Mutate a message using a mutator.
     * @param eventId The ID of the message to mutate.
     * @param mutatorId The ID of the mutator to apply.
     */
    @PostMapping("/event/{eventId}/mutate/{mutatorId}")
    public void mutateMessage(@PathVariable Long eventId, @PathVariable Long mutatorId) {
        simulatorService.getScenarioExecutor().getTransport().applyMutation(eventId, mutatorId);
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

    /**
     * Schedule N actions, according to the scheduler policy.
     * @param numActions The number of events to schedule.
     * @throws Exception If the scheduler fails to schedule the next event.
     */
    @PostMapping("/scheduler/next")
    public void scheduleNext(@RequestParam(required = false, defaultValue = "1") Integer numActions) throws Exception {
        for (int i = 0; i < numActions; i++) {
            simulatorService.getScenarioExecutor().getScheduler().scheduleNext();
        }
    }

    @GetMapping("/adob")
    public AdobCache getAdob() {
        return simulatorService.getScenarioExecutor().getAdobOracle().getRoot();
    }

    @GetMapping("/adob/caches")
    public Collection<AdobCache> getAllAdobCaches() {
        return simulatorService.getScenarioExecutor().getAdobOracle().getCaches().values();
    }

    @GetMapping("/adob/caches/{cacheId}")
    public AdobCache getAdobCache(@PathVariable Long cacheId) {
        return simulatorService.getScenarioExecutor().getAdobOracle().getCaches().get(cacheId);
    }

    @GetMapping("/scenarios")
    public List<String> getScenarios() {
        return scenarioFactoryService.getScenarioIds();
    }

    @GetMapping("/schedulers")
    public List<String> getSchedulers() {
        return schedulerFactoryService.getSchedulerIds();
    }

    @GetMapping("/saved-schedules")
    public int getNumSavedSchedules() {
        return schedulesService.getSchedules().size();
    }

    @GetMapping("/saved-schedules/{scheduleId}")
    public Schedule getSavedSchedule(@PathVariable int scheduleId) {
        return schedulesService.getSchedules().get(scheduleId);
    }
}
