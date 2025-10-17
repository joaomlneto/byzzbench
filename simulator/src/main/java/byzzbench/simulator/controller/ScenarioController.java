package byzzbench.simulator.controller;

import byzzbench.simulator.*;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.state.adob.AdobCache;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MailboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for CRUD operations on simulations.
 */
@RestController
@RequiredArgsConstructor
@Log
public class ScenarioController {
    private final MessageMutatorService messageMutatorService;
    private final ScenarioService scenarioService;

    /**
     * Get a given scenario
     *
     * @param scenarioId The ID of the scenario
     * @return the scenario
     */
    @GetMapping("/scenarios/{scenarioId}")
    public Scenario getScenario(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId);
    }

    /**
     * Get the currently active scenarios
     *
     * @return the ID of active scenarios
     */
    @GetMapping("/scenarios")
    public Set<Long> getScenarios() {
        return scenarioService.getMaterializedScheduleIds();
    }

    /**
     * Get the list of all currently materialized scenarios
     *
     * @return a list of scenario names
     */
    @GetMapping("/scenario-factories")
    public List<String> getScenarioFactoryIds() {
        return scenarioService.getScenarioFactoryIds();
    }

    /**
     * Get the schedule of a given scenario
     *
     * @param scenarioId The ID of the scenario to get the schedule for.
     * @return The schedule for the scenario with the given ID.
     */
    @GetMapping("/scenarios/{scenarioId}/schedule")
    public Schedule getScenarioSchedule(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getSchedule();
    }

    @GetMapping("/scenarios/{scenarioId}/nodes")
    public SortedSet<String> getScenarioNodes(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getNodes()
                .navigableKeySet();
    }

    @GetMapping("/scenarios/{scenarioId}/nodes/{nodeId}")
    public Node getScenarioNode(@PathVariable long scenarioId, @PathVariable String nodeId) {
        return scenarioService.getScenarioById(scenarioId).getNode(nodeId);
    }

    /**
     * Get the list of event IDs in the mailbox of the node with the given ID.
     *
     * @param scenarioId The ID of the scenario
     * @param nodeId     The ID of the node to get the mailbox of.
     * @param type       The type of the message to filter by.
     * @return The list of event IDs in the mailbox of the node with the given ID.
     */
    @GetMapping("/scenarios/{scenarioId}/nodes/{nodeId}/mailbox")
    public List<Long> getScenarioNodeMailbox(@PathVariable long scenarioId, @PathVariable String nodeId, @RequestParam(required = false) String type) {
        return scenarioService.getScenarioById(scenarioId)
                .getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(e -> type == null || e.getClass().getSimpleName().equals(type))
                .filter(e -> e instanceof MailboxEvent me && me.getRecipientId().equals(nodeId))
                .map(Event::getEventId)
                .toList();
    }

    @GetMapping("/scenarios/{scenarioId}/clients")
    public SortedSet<String> getScenarioClients(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getClients()
                .navigableKeySet();
    }

    @GetMapping("/scenarios/{scenarioId}/clients/{clientId}")
    public Client getScenarioClient(@PathVariable long scenarioId, @PathVariable String clientId) {
        NavigableMap<String, Client> clients = scenarioService.getScenarioById(scenarioId).getClients();
        if (!clients.containsKey(clientId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }
        return clients.get(clientId);
    }

    @GetMapping("/scenarios/{scenarioId}/replicas")
    public SortedSet<String> getScenarioReplicas(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getReplicas()
                .navigableKeySet();
    }

    @GetMapping("/scenarios/{scenarioId}/replicas/{replicaId}")
    public Replica getScenarioReplica(@PathVariable long scenarioId, @PathVariable String replicaId) {
        NavigableMap<String, Replica> replicas = scenarioService.getScenarioById(scenarioId).getReplicas();
        if (!replicas.containsKey(replicaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }
        return replicas.get(replicaId);
    }

    /**
     * Get the list of faulty replicas in the scenario
     *
     * @param scenarioId The ID of the scenario
     * @return the set of IDs of faulty replicas
     */
    @GetMapping("/scenarios/{scenarioId}/faulty-replicas")
    public SortedSet<String> getScenarioFaultyReplicaIds(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getFaultyReplicaIds();
    }

    /**
     * Get the list of faults configured in the scenario
     *
     * @param scenarioId The ID of the scenario
     * @return the set of IDs of faulty replicas
     */
    @GetMapping("/scenarios/{scenarioId}/faults")
    public SortedSet<String> getScenarioFaults(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getFaults().stream()
                .map(Fault::getId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Get the list of enabled mutators for the scenario.
     * FIXME: this needs refactoring
     *
     * @param scenarioId The ID of the scenario
     * @return the set of mutator IDs enabled
     */
    @GetMapping("/scenarios/{scenarioId}/mutators")
    public SortedSet<String> getScenarioMutators(@PathVariable long scenarioId) {
        return messageMutatorService
                .getMutatorsMap()
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Get the list of network partitions in the scenario
     *
     * @param scenarioId The ID of the scenario
     * @return the set of mutator IDs enabled
     */
    @GetMapping("/scenarios/{scenarioId}/partitions")
    public SortedMap<String, Integer> getScenarioPartitions(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getTransport()
                .getRouter()
                .getPartitions();
    }

    /**
     * Get the current distributed system state as per the ADoB oracle.
     *
     * @return The current distributed system state.
     */
    @GetMapping("/scenarios/{scenarioId}/adob")
    public AdobCache getAdob(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getObservers().stream()
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
    @GetMapping("/scenarios/{scenarioId}/adob/caches")
    public Collection<AdobCache> getAllAdobCaches(@PathVariable long scenarioId) {
        AdobDistributedState adob = scenarioService.getScenarioById(scenarioId).getObservers().stream()
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
    @GetMapping("/scenarios/{scenarioId}/adob/caches/{cacheId}")
    public AdobCache getAdobCache(@PathVariable long scenarioId, @PathVariable Long cacheId) {
        AdobDistributedState adob = scenarioService.getScenarioById(scenarioId).getObservers().stream()
                .filter(AdobDistributedState.class::isInstance)
                .map(o -> (AdobDistributedState) o)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ADoB oracle not found"));
        return adob.getCaches().get(cacheId);
    }

    /**
     * Get the list of all defined network faults in the current scenario.
     *
     * @return a sorted set of network fault names
     */
    @GetMapping("/scenarios/{scenarioId}/network-faults")
    public SortedSet<String> getNetworkFaults(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getTransport().getNetworkFaults().keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping("/scenarios/{scenarioId}/automatic-faults")
    public SortedSet<String> getAutomaticFaults(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId).getTransport().getAutomaticFaults().keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @DeleteMapping("/scenarios/{scenarioId}/automatic-faults")
    public void deleteAutomaticFaults(@PathVariable long scenarioId) {
        scenarioService.getScenarioById(scenarioId).getTransport().getAutomaticFaults().clear();
    }

    @GetMapping("/scenarios/{scenarioId}/enabled-network-faults")
    public SortedSet<String> getEnabledNetworkFaults(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getTransport()
                .getEnabledNetworkFaults()
                .stream()
                .map(Fault::getId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping("/scenarios/{scenarioId}/network-faults/{faultId}")
    public Fault getNetworkFault(@PathVariable long scenarioId, @PathVariable String faultId) {
        return scenarioService.getScenarioById(scenarioId).getTransport().getNetworkFault(faultId);
    }

    @GetMapping("/scenarios/{scenarioId}/automatic-faults/{faultId}")
    public Fault getAutomaticFault(@PathVariable long scenarioId, @PathVariable String faultId) {
        return scenarioService.getScenarioById(scenarioId).getTransport().getAutomaticFaults().get(faultId);
    }

    @PostMapping("/scenarios/{scenarioId}/network-fault/{faultId}")
    public void enableNetworkFault(@PathVariable long scenarioId, @PathVariable String faultId) {
        scenarioService.getScenarioById(scenarioId).getTransport().applyFault(faultId);
    }

    @GetMapping("/scenarios/{scenarioId}/predicates")
    public List<ScenarioPredicate> getScenarioPredicates(@PathVariable long scenarioId) {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return scenario.getInvariants();
    }

    /**
     * Get the list of all event IDs in the scenario.
     *
     * @return The list of all event IDs in the scenario.
     */
    @GetMapping("/scenarios/{scenarioId}/events")
    public List<Long> getEvents(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
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
    @GetMapping("/scenarios/{scenarioId}/events/{eventId}")
    public Event getEvent(@PathVariable long scenarioId, @PathVariable Long eventId) {
        return scenarioService.getScenarioById(scenarioId)
                .getTransport()
                .getEvents()
                .get(eventId);
    }

    /**
     * Get the list of event IDs in the QUEUED state.
     *
     * @return The list of event IDs in the QUEUED state.
     */
    @GetMapping("/scenarios/{scenarioId}/events/queued")
    public List<Long> getQueuedMessages(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
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
    @GetMapping("/scenarios/{scenarioId}/events/dropped")
    public List<Long> getDroppedMessages(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
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
    @GetMapping("/scenarios/{scenarioId}/events/delivered")
    public List<Long> getDeliveredMessages(@PathVariable long scenarioId) {
        return scenarioService.getScenarioById(scenarioId)
                .getTransport()
                .getEventsInState(Event.Status.DELIVERED)
                .stream()
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Get the event with the given ID.
     *
     * @param eventId The ID of the event to get.
     * @return The event with the given ID.
     */
    @GetMapping("/scenarios/{scenarioId}/event/{eventId}")
    public Event getMessage(@PathVariable long scenarioId, @PathVariable Long eventId) {
        return scenarioService.getScenarioById(scenarioId)
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
    @GetMapping("/scenarios/{scenarioId}/event/{eventId}/mutators")
    public List<String> getMessageMutators(@PathVariable long scenarioId, @PathVariable Long eventId) {
        Event e = scenarioService.getScenarioById(scenarioId)
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
    @PostMapping("/scenarios/{scenarioId}/event/{eventId}/deliver")
    public void deliverMessage(@PathVariable long scenarioId, @PathVariable Long eventId) throws Exception {
        scenarioService.getScenarioById(scenarioId).getTransport().deliverEvent(eventId);
    }

    /**
     * Drop the event with the given ID.
     *
     * @param eventId The ID of the event to drop.
     */
    @PostMapping("/scenarios/{scenarioId}/event/{eventId}/drop")
    public void dropMessage(@PathVariable long scenarioId, @PathVariable Long eventId) {
        scenarioService.getScenarioById(scenarioId).getTransport().dropEvent(eventId);
    }

    /**
     * Mutate a message using a mutator.
     *
     * @param eventId   The ID of the message to mutate.
     * @param mutatorId The ID of the mutator to apply.
     */
    @PostMapping("/scenarios/{scenarioId}/event/{eventId}/mutate/{mutatorId}")
    public void mutateMessage(@PathVariable long scenarioId, @PathVariable Long eventId, @PathVariable String mutatorId) {
        MessageMutationFault mutator = this.messageMutatorService.getMutator(mutatorId);
        scenarioService.getScenarioById(scenarioId).getTransport().applyMutation(eventId, mutator);
    }

    /**
     * Reset the scenario to its initial state.
     *
     * @param scenarioId The ID of the scenario to reset.
     */
    @PostMapping("/scenarios/{scenarioId}/reset")
    public void resetScenario(@PathVariable long scenarioId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Delete the scenario with the given ID.
     *
     * @param scenarioId The ID of the scenario to delete.
     */
    @DeleteMapping("/scenarios/{scenarioId}")
    public void deleteScenario(@PathVariable long scenarioId) {
        scenarioService.storeSchedule(scenarioId);
    }

    /**
     * Get available actions for the scenario.
     *
     * @param scenarioId The ID of the scenario.
     * @return The list of available actions.
     */
    @GetMapping("/scenarios/{scenarioId}/actions")
    public List<? extends Action> getScenarioAvailableActions(@PathVariable long scenarioId) {
        List<? extends Action> actions = scenarioService.getScenarioById(scenarioId).getAvailableActions();
        log.info("Available actions: " + actions.stream().map(Action::toString).collect(Collectors.joining(", ")));
        return actions;
    }

}
