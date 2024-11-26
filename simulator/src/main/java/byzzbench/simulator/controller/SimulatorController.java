package byzzbench.simulator.controller;

import byzzbench.simulator.Client;
import byzzbench.simulator.Node;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.service.SchedulerFactoryService;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.state.adob.AdobCache;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MailboxEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * REST API for interacting with the simulator.
 */
@RestController
@RequiredArgsConstructor
public class SimulatorController {
    private final MessageMutatorService messageMutatorService;
    private final SimulatorService simulatorService;
    private final ScenarioService scenarioService;
    private final SchedulerFactoryService schedulerFactoryService;
    private final ByzzBenchConfig byzzBenchConfig;

    /**
     * Get the status of the simulator.
     *
     * @return The status of the simulator.
     */
    @GetMapping("/status")
    public String getStatus() {
        return "Running";
    }

    /**
     * Get the list of available client IDs in the current scenario.
     *
     * @return The list of client IDs.
     */
    @GetMapping("/clients")
    public SortedSet<String> getClients() {
        return simulatorService.getScenario()
                .getClients()
                .navigableKeySet();
    }

    /**
     * Get the client with the given ID.
     *
     * @param clientId The ID of the client to get.
     * @return The client with the given ID.
     */
    @GetMapping("/client/{clientId}")
    public Client getClient(@PathVariable String clientId) {
        return simulatorService.getScenario().getClients().get(clientId);
    }

    /**
     * Get the list of available node IDs in the current scenario.
     *
     * @return The list of node IDs.
     */
    @GetMapping("/nodes")
    public SortedSet<String> getNodes() {
        return simulatorService.getScenario().getNodes().navigableKeySet();
    }

    /**
     * Get the list of available node IDs in the current scenario.
     *
     * @return The list of node IDs.
     */
    @GetMapping("/replicas")
    public SortedSet<String> getReplicas() {
        return simulatorService.getScenario().getReplicas().navigableKeySet();
    }

    /**
     * Get the internal state of the node with the given ID.
     *
     * @param nodeId The ID of the node to get.
     * @return The internal state of the node with the given ID.
     */
    @GetMapping("/node/{nodeId}")
    public Node getNode(@PathVariable String nodeId) {
        return simulatorService.getScenario().getNode(nodeId);
    }

    /**
     * Get the list of event IDs in the mailbox of the node with the given ID.
     *
     * @param nodeId The ID of the node to get the mailbox of.
     * @param type   The type of the message to filter by.
     * @return The list of event IDs in the mailbox of the node with the given ID.
     */
    @GetMapping("/node/{nodeId}/mailbox")
    public List<Long> getNodeMailbox(@PathVariable String nodeId,
                                     @RequestParam(required = false) String type) {
        return simulatorService.getScenario()
                .getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(e -> type == null || e.getClass().getSimpleName().equals(type))
                .filter(e -> e instanceof MailboxEvent me && me.getRecipientId().equals(nodeId))
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Get the list of all event IDs in the scenario.
     *
     * @return The list of all event IDs in the scenario.
     */
    @GetMapping("/events")
    public List<Long> getEvents() {
        return simulatorService.getScenario()
                .getTransport()
                .getEvents()
                .keySet()
                .stream()
                .toList();
    }

    /**
     * Get the event with the given ID.
     *
     * @param eventId The ID of the event to get.
     * @return The event with the given ID.
     */
    @GetMapping("/events/{eventId}")
    public Event getEvent(@PathVariable Long eventId) {
        return simulatorService.getScenario()
                .getTransport()
                .getEvents()
                .get(eventId);
    }

    /**
     * Get the list of event IDs in the QUEUED state.
     *
     * @return The list of event IDs in the QUEUED state.
     */
    @GetMapping("/events/queued")
    public List<Long> getQueuedMessages() {
        return simulatorService.getScenario()
                .getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Get the list of event IDs in the DROPPED state.
     *
     * @return The list of event IDs in the DROPPED state.
     */
    @GetMapping("/events/dropped")
    public List<Long> getDroppedMessages() {
        return simulatorService.getScenario()
                .getTransport()
                .getEventsInState(Event.Status.DROPPED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Get the list of event IDs in the DELIVERED state.
     *
     * @return The list of event IDs in the DELIVERED state.
     */
    @GetMapping("/events/delivered")
    public List<Long> getDeliveredMessages() {
        return simulatorService.getScenario()
                .getTransport()
                .getEventsInState(Event.Status.DELIVERED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Get the current schedule of the simulator.
     *
     * @return The list of delivered event IDs in order.
     */
    @GetMapping("/schedule")
    public Schedule getSchedule() {
        return simulatorService.getScenario().getSchedule();
    }

    /**
     * Materializes a given schedule in a given scenario.
     *
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

    /**
     * Get the event with the given ID.
     *
     * @param eventId The ID of the event to get.
     * @return The event with the given ID.
     */
    @GetMapping("/event/{eventId}")
    public Event getMessage(@PathVariable Long eventId) {
        return simulatorService.getScenario()
                .getTransport()
                .getEvents()
                .get(eventId);
    }

    /**
     * Get the list of mutators that can be applied to the event with the given ID.
     *
     * @param eventId The ID of the event to get mutators for.
     * @return The list of mutators that can be applied to the event with the given ID.
     */
    @GetMapping("/event/{eventId}/mutators")
    public List<String> getMessageMutators(@PathVariable Long eventId) {
        Event e = simulatorService.getScenario()
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

    /**
     * Deliver the event with the given ID.
     *
     * @param eventId The ID of the event to deliver.
     * @throws Exception If the event cannot be delivered.
     */
    @PostMapping("/event/{eventId}/deliver")
    public void deliverMessage(@PathVariable Long eventId) throws Exception {
        simulatorService.getScenario().getTransport().deliverEvent(eventId);
    }

    /**
     * Drop the event with the given ID.
     *
     * @param eventId The ID of the event to drop.
     */
    @PostMapping("/event/{eventId}/drop")
    public void dropMessage(@PathVariable Long eventId) {
        simulatorService.getScenario().getTransport().dropEvent(eventId);
    }

    /**
     * Mutate a message using a mutator.
     *
     * @param eventId   The ID of the message to mutate.
     * @param mutatorId The ID of the mutator to apply.
     */
    @PostMapping("/event/{eventId}/mutate/{mutatorId}")
    public void mutateMessage(@PathVariable Long eventId, @PathVariable String mutatorId) {
        MessageMutationFault mutator = this.messageMutatorService.getMutator(mutatorId);
        simulatorService.getScenario().getTransport().applyMutation(eventId, mutator);
    }

    /**
     * Get the list of enabled mutators.
     *
     * @return The list of enabled mutators.
     */
    @GetMapping("/mutators")
    public SortedSet<String> getMutators() {
        return messageMutatorService
                .getMutatorsMap()
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Get the mutator with the given ID.
     *
     * @param mutatorId The ID of the mutator to get.
     * @return The mutator with the given ID.
     */
    @GetMapping("/mutators/{mutatorId}")
    public MessageMutationFault getMutator(@PathVariable String mutatorId) {
        return messageMutatorService
                .getMutator(mutatorId);
    }

    /**
     * Reset the scenario to its initial state.
     */
    @PostMapping("/reset")
    public void reset() {
        simulatorService.resetScenario();
    }

    /**
     * Schedule N actions, according to the scheduler policy.
     *
     * @param numActions The number of events to schedule.
     * @throws Exception If the scheduler fails to schedule the next event.
     */
    @PostMapping("/scheduler/next")
    public void scheduleNext(@RequestParam(required = false, defaultValue = "1") Integer numActions) throws Exception {
        for (int i = 0; i < numActions; i++) {
            simulatorService.invokeScheduleNext();
        }
    }

    /**
     * Get the current distributed system state as per the ADoB oracle.
     *
     * @return The current distributed system state.
     */
    @GetMapping("/adob")
    public AdobCache getAdob() {
        return simulatorService.getScenario().getObservers().stream()
                .filter(AdobDistributedState.class::isInstance)
                .map(o -> (AdobDistributedState) o)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ADoB oracle not found"))
                .getRoot();
    }

    /**
     * Get the list of caches in the ADoB oracle.
     *
     * @return The list of caches in the ADoB oracle.
     */
    @GetMapping("/adob/caches")
    public Collection<AdobCache> getAllAdobCaches() {
        AdobDistributedState adob = simulatorService.getScenario().getObservers().stream()
                .filter(AdobDistributedState.class::isInstance)
                .map(o -> (AdobDistributedState) o)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ADoB oracle not found"));
        return adob.getCaches().values();
    }

    /**
     * Get the cache with the given ID in the ADoB oracle.
     *
     * @param cacheId The ID of the cache to get.
     * @return The cache with the given ID in the ADoB oracle.
     */
    @GetMapping("/adob/caches/{cacheId}")
    public AdobCache getAdobCache(@PathVariable Long cacheId) {
        AdobDistributedState adob = simulatorService.getScenario().getObservers().stream()
                .filter(AdobDistributedState.class::isInstance)
                .map(o -> (AdobDistributedState) o)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ADoB oracle not found"));
        return adob.getCaches().get(cacheId);
    }

    /**
     * Get the list of scenarios available in the simulator.
     *
     * @return The list of scenario IDs.
     */
    @GetMapping("/scenarios")
    public List<String> getScenarios() {
        return scenarioService.getScenarioIds();
    }

    /**
     * Change the scenario to the one with the given ID.
     *
     * @param scenarioId The ID of the scenario to change to.
     */
    @PostMapping("/change-scenario")
    public void changeScenario(@RequestParam String scenarioId, @RequestBody JsonNode params) {
        simulatorService.changeScenario(scenarioId, params);
    }

    /**
     * Get the ID of the current scenario.
     *
     * @return The ID of the current scenario.
     */
    @GetMapping("/current-scenario-id")
    public String getCurrentScenarioId() {
        return simulatorService.getScenario().getId();
    }

    /**
     * Get the list of schedulers available in the simulator.
     *
     * @return The list of scheduler IDs.
     */
    @GetMapping("/schedulers")
    public List<String> getSchedulers() {
        return schedulerFactoryService.getSchedulerIds();
    }

    /**
     * Get the list of schedulers available in the simulator.
     *
     * @return The list of scheduler IDs.
     */
    @GetMapping("/scheduler")
    public Scheduler getScheduler() {
        return simulatorService.getScenario().getScheduler();
    }

    /**
     * Get the list of saved schedules.
     *
     * @return The
     */
    @GetMapping("/saved-schedules")
    public List<Integer> getNumSavedSchedules() {
        return IntStream.range(0, scenarioService.getScenarios().size())
                .boxed()
                .toList();
    }

    /**
     * Get the list of saved schedules.
     *
     * @return The list of saved schedules.
     */
    @GetMapping("/saved-schedules/buggy")
    public List<Integer> getBuggyScheduleIds() {
        List<Scenario> scenarios = scenarioService.getScenarios();
        return scenarios.stream()
                .map(Scenario::getSchedule)
                .filter(Schedule::isBuggy)
                .map(scenarios::indexOf)
                .toList();
    }

    /**
     * Get a given saved schedule.
     *
     * @param scenarioId The ID of the scenario to get the schedule for.
     * @return The schedule for the scenario with the given ID.
     */
    @GetMapping("/scenario/{scenarioId}/schedule")
    public Schedule getScenarioSchedule(@PathVariable int scenarioId) {
        return scenarioService.getScenarios().get(scenarioId).getSchedule();
    }

    // endpoint to run the current scenario N times
    // parameters: numRuns, eventsPerRun
    // returns: list of schedules
    @PostMapping("/start")
    public void start() {
        simulatorService.start();
    }

    /**
     * Stop the current scenario.
     */
    @PostMapping("/stop")
    public void stop() {
        if (!simulatorService.getMode().equals(SimulatorService.SimulatorServiceMode.RUNNING)) {
            throw new IllegalStateException("Simulator is not running");
        }
        simulatorService.stop();
    }

    @GetMapping("/simulator/mode")
    public SimulatorService.SimulatorServiceMode getMode() {
        return simulatorService.getMode();
    }

    @GetMapping("/network-faults")
    public SortedSet<String> getNetworkFaults() {
        return simulatorService.getScenario().getTransport().getNetworkFaults().keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping("/automatic-faults")
    public SortedSet<String> getAutomaticFaults() {
        return simulatorService.getScenario().getTransport().getAutomaticFaults().keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping("/enabled-network-faults")
    public SortedSet<String> getEnabledNetworkFaults() {
        return simulatorService
                .getScenario()
                .getTransport()
                .getEnabledNetworkFaults()
                .stream()
                .map(Fault::getId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping("/network-faults/{faultId}")
    public Fault getNetworkFault(@PathVariable String faultId) {
        return simulatorService.getScenario().getTransport().getNetworkFault(faultId);
    }

    @GetMapping("/automatic-faults/{faultId}")
    public Fault getAutomaticFault(@PathVariable String faultId) {
        return simulatorService.getScenario().getTransport().getAutomaticFaults().get(faultId);
    }

    @PostMapping("/network-fault/{faultId}")
    public void enableNetworkFault(@PathVariable String faultId) {
        simulatorService.getScenario().getTransport().applyFault(faultId);
    }

    @GetMapping("/partitions")
    public SortedMap<String, Integer> getPartitions() {
        return simulatorService.getScenario()
                .getTransport()
                .getRouter()
                .getPartitions();
    }

    @GetMapping("/scenario")
    public Scenario getScenario() {
        return simulatorService.getScenario();
    }

    @GetMapping("/scenario/predicates")
    public SortedMap<String, Boolean> getScenarioPredicates() {
        return new TreeMap<>(simulatorService.getScenario().getInvariants()
                .stream()
                .collect(Collectors.toMap(ScenarioPredicate::getId, p -> p.test(simulatorService.getScenario()))));
    }

    @GetMapping("/config")
    public ByzzBenchConfig getConfig() {
        return byzzBenchConfig;
    }
}
