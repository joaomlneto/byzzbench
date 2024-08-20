package byzzbench.simulator.controller;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.service.*;
import byzzbench.simulator.state.adob.AdobCache;
import byzzbench.simulator.transport.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * REST API for interacting with the simulator.
 */
@RestController
@RequiredArgsConstructor
public class SimulatorController {
    private final MessageMutatorService messageMutatorService;
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
    public Schedule getSchedule() {
        return simulatorService.getScenarioExecutor()
                .getTransport()
                .getSchedule();
    }

    /**
     * Materializes a given schedule in a given scenario.
     * @param schedule The schedule to materialize.
     */
    @PutMapping("/schedule")
    public void materializeSchedule(@RequestBody Schedule schedule) {
        // FIXME: the deserialization of the schedule is not working
        System.out.println("Materializing Schedule: " + schedule);
        throw new UnsupportedOperationException("Materializing schedules is not supported yet.");
        /*
        ScenarioExecutor<?> scenarioExecutor = simulatorService.getScenarioExecutor();

        // set scenario
        simulatorService.changeScenario(schedule.getScenarioId());

        System.out.println("SO FAR SO GOOD!");

        // set schedule
        for (Event event : schedule.getEvents()) {
            Event e = scenarioExecutor.getTransport().getEvent(event.getEventId());

            if (e == null) {
                throw new IllegalArgumentException("Event not found: " + event.getEventId());
            }

            if (e.getStatus() != Event.Status.QUEUED) {
                throw new IllegalArgumentException("Event is not in QUEUED state: " + event.getEventId());
            }

            // TODO: apply the event
        }*/
    }

    @GetMapping("/event/{eventId}")
    public Event getMessage(@PathVariable Long eventId) {
        return simulatorService.getScenarioExecutor()
                .getTransport()
                .getEvents()
                .get(eventId);
    }

    @GetMapping("/event/{eventId}/mutators")
    public List<String> getMessageMutators(@PathVariable Long eventId) {
        Event e = simulatorService.getScenarioExecutor()
                .getTransport()
                .getEvents()
                .get(eventId);

        // if the event is not found, throw an exception
        if (e == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }

        return messageMutatorService.getMutatorsForEvent(e)
                .stream()
                .map(MessageMutationFault::getId)
                .toList();
    }

    @PostMapping("/event/{eventId}/deliver")
    public void deliverMessage(@PathVariable Long eventId) throws Exception {
        simulatorService.getScenarioExecutor().getTransport().deliverEvent(eventId);
    }

    @PostMapping("/event/{eventId}/drop")
    public void dropMessage(@PathVariable Long eventId) {
        simulatorService.getScenarioExecutor().getTransport().dropEvent(eventId);
    }

    /**
     * Mutate a message using a mutator.
     * @param eventId The ID of the message to mutate.
     * @param mutatorId The ID of the mutator to apply.
     */
    @PostMapping("/event/{eventId}/mutate/{mutatorId}")
    public void mutateMessage(@PathVariable Long eventId, @PathVariable String mutatorId) {
        Fault<?> f = this.messageMutatorService.getMutator(mutatorId);
        simulatorService.getScenarioExecutor().getTransport().applyMutation(eventId, f);
    }

    @GetMapping("/mutators")
    public Set<String> getMutators() {
        return messageMutatorService
                .getMutatorsMap()
                .keySet();
    }

    @GetMapping("/mutators/{mutatorId}")
    public MessageMutationFault<? extends Serializable> getMutator(@PathVariable String mutatorId) {
        return messageMutatorService
                .getMutator(mutatorId);
    }

    @PostMapping("/reset")
    public void reset() {
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
            simulatorService.invokeScheduleNext();
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

    @PostMapping("/change-scenario")
    public void changeScenario(@RequestParam String scenarioId) {
        simulatorService.changeScenario(scenarioId);
    }

    @GetMapping("/current-scenario-id")
    public String getCurrentScenarioId() {
        return simulatorService.getScenarioExecutor().getId();
    }

    @GetMapping("/schedulers")
    public List<String> getSchedulers() {
        return schedulerFactoryService.getSchedulerIds();
    }

    @GetMapping("/saved-schedules")
    public List<Integer> getNumSavedSchedules() {
        return IntStream.range(0, schedulesService.getSchedules().size())
                .boxed()
                .toList();
    }

    @GetMapping("/saved-schedules/buggy")
    public List<Integer> getBuggyScheduleIds() {
        return schedulesService.getSchedules()
                .stream()
                .filter(Schedule::isBuggy)
                .map(schedulesService.getSchedules()::indexOf)
                .toList();
    }

    @GetMapping("/saved-schedules/{scheduleId}")
    public Schedule getSavedSchedule(@PathVariable int scheduleId) {
        return schedulesService.getSchedules().get(scheduleId);
    }

    // endpoint to run the current scenario N times
    // parameters: numRuns, eventsPerRun
    // returns: list of schedules
    @PostMapping("/start")
    public void start(@RequestParam int eventsPerRun) {
        //System.out.println("Starting simulator with " + eventsPerRun + " events per run");
        simulatorService.start(eventsPerRun);
    }

    @PostMapping("/stop")
    public void stop() {
        if (!simulatorService.getMode().equals(SimulatorService.SimulatorServiceMode.RUNNING)) {
            throw new IllegalStateException("Simulator is not running");
        }
        //System.out.println("Stopping simulator");
        simulatorService.stop();
    }

    @GetMapping("/simulator/mode")
    public SimulatorService.SimulatorServiceMode getMode() {
        return simulatorService.getMode();
    }

    @GetMapping("/network-faults")
    public Set<String> getNetworkFaults() {
        return simulatorService.getScenarioExecutor().getTransport().getNetworkFaults().keySet();
    }

    @GetMapping("/enabled-network-faults")
    public Set<String> getEnabledNetworkFaults() {
        return simulatorService
                .getScenarioExecutor()
                .getTransport()
                .getEnabledNetworkFaults()
                .stream()
                .map(Fault::getId)
                .collect(Collectors.toSet());
    }

    @GetMapping("/network-faults/{faultId}")
    public Fault<? extends Serializable> getNetworkFault(@PathVariable String faultId) {
        return simulatorService.getScenarioExecutor().getTransport().getNetworkFault(faultId);
    }

    @PostMapping("/network-fault/{faultId}")
    public void enableNetworkFault(@PathVariable String faultId) {
        simulatorService.getScenarioExecutor().getTransport().applyFault(faultId);
    }

    @GetMapping("/partitions")
    public Map<String, Integer> getPartitions() {
        return simulatorService.getScenarioExecutor()
                .getTransport()
                .getRouter()
                .getPartitions();
    }
}
