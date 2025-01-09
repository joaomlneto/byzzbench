package byzzbench.simulator.transport;

import byzzbench.simulator.Client;
import byzzbench.simulator.Node;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Transport layer for the simulator.
 * <p>
 * This class is responsible for handling events (messages and timeouts).
 * It also provides methods for sending messages, setting timeouts, and applying
 * faults.
 */
@Log
@RequiredArgsConstructor
public class Transport {
    /**
     * The scenario executor for the transport layer.
     */
    @JsonIgnore
    @Getter(onMethod_ = {@Synchronized})
    private final Scenario scenario;

    /**
     * The sequence number for events.
     */
    @JsonIgnore
    private final AtomicLong eventSeqNum = new AtomicLong(1);

    /**
     * Map of event ID to the {@link Event} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final SortedMap<Long, Event> events = new TreeMap<>();

    /**
     * Map of automatic fault id to the {@link Fault} object. This is used to
     * apply faulty behaviors automatically  to the system whenever their predicate is satisfied.
     */
    @JsonIgnore
    @Getter(onMethod_ = {@Synchronized})
    private final SortedMap<String, Fault> automaticFaults = new TreeMap<>();

    /**
     * Map of network fault id to the {@link Fault} object. This is used to
     * apply network faults to the system. These faults are NOT applied automatically.
     */
    @JsonIgnore
    @Getter(onMethod_ = {@Synchronized})
    private final SortedMap<String, Fault> networkFaults = new TreeMap<>();

    /**
     * The router for managing partitions.
     */
    @JsonIgnore
    @Getter(onMethod_ = {@Synchronized})
    private final Router router = new Router();

    /**
     * List of observers for the transport layer.
     */
    @JsonIgnore
    private final List<TransportObserver> observers = new ArrayList<>();

    /**
     * Adds an observer to the transport layer.
     *
     * @param observer The observer to add.
     */
    public synchronized void addObserver(TransportObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Removes an observer from the transport layer.
     *
     * @param observer The observer to remove.
     */
    public synchronized void removeObserver(TransportObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Adds a network fault to the transport layer.
     *
     * @param fault The fault to add.
     */
    public synchronized void addFault(Fault fault, boolean triggerAutomatically) {
        if (triggerAutomatically) {
            this.automaticFaults.put(fault.getId(), fault);
        } else {
            this.networkFaults.put(fault.getId(), fault);
        }
    }

    /**
     * Sends a request from a client to a replica.
     *
     * @param sender    The ID of the client sending the request
     * @param operation The operation to send
     * @param recipient The ID of the replica receiving the request
     */
    public synchronized void sendClientRequest(String sender, Serializable operation, String recipient) {
        // assert that the sender exists
        if (!this.scenario.getClients().containsKey(sender)) {
            throw new IllegalArgumentException("Client not found: " + sender);
        }

        // assert that the recipient exists
        if (!this.scenario.getNodes().containsKey(recipient)) {
            throw new IllegalArgumentException("Replica not found: " + recipient);
        }

        Event event = ClientRequestEvent.builder()
                .eventId(this.eventSeqNum.getAndIncrement())
                .senderId(sender)
                .recipientId(recipient)
                .payload(new DefaultClientRequestPayload(operation))
                .build();
        this.appendEvent(event);
    }

    /**
     * Sends a response from a replica to a client.
     *
     * @param sender    The ID of the replica sending the response
     * @param response  The response to send
     * @param recipient The ID of the client receiving the response
     */
    public synchronized void sendClientResponse(Node sender, MessagePayload response, String recipient) {
        // assert that the sender exists
        if (!this.scenario.getNodes().containsKey(sender.getId())) {
            throw new IllegalArgumentException("Replica not found: " + sender);
        }

        if (!this.scenario.getClients().containsKey(recipient)) {
            throw new IllegalArgumentException("Client not found: " + recipient);
        }

        // deliver the reply directly to the client to handle
        Client c = this.scenario.getClients().get(recipient);
        c.handleMessage(sender.getId(), response);
    }

    /**
     * Sends a message between two replicas.
     *
     * @param sender    The ID of the replica sending the message
     * @param message   The payload of the message to send
     * @param recipient The ID of the replica receiving the message
     */
    public synchronized void sendMessage(Node sender, MessagePayload message,
                                         String recipient) {
        this.multicast(sender, new TreeSet<>(Set.of(recipient)), message);
    }

    /**
     * Gets all events in a given state.
     *
     * @param status The state to filter by
     * @return A list of events in the given state
     */
    public synchronized List<Event> getEventsInState(Event.Status status) {
        return this.events.values()
                .stream()
                .filter(m -> m.getStatus() == status)
                .toList();
    }

    /**
     * Append an event to the transport layer.
     *
     * @param event The event to append
     */
    private synchronized void appendEvent(Event event) {
        // add the event to the map
        this.events.put(event.getEventId(), event);

        // apply automatic faults
        this.automaticFaults.values()
                .forEach(f -> f.testAndAccept(new FaultContext(this.scenario, event)));

        // notify observers
        this.observers.forEach(o -> o.onEventAdded(event));
    }

    /**
     * Multicasts a message to a set of recipients.
     *
     * @param sender     The sender node
     * @param recipients The set of recipient IDs
     * @param payload    The payload of the message
     */
    public synchronized void multicast(Node sender, SortedSet<String> recipients,
                                       MessagePayload payload) {
        for (String recipient : recipients) {
            long messageId = this.eventSeqNum.getAndIncrement();
            MessageEvent messageEvent = MessageEvent.builder()
                    .eventId(messageId)
                    .senderId(sender.getId())
                    .recipientId(recipient)
                    .payload(payload)
                    .build();
            this.appendEvent(messageEvent);

            // if they don't have connectivity, drop it directly
            if (!router.haveConnectivity(sender.getId(), recipient)) {
                this.dropEvent(messageId);
            }
        }
    }

    public synchronized void deliverEvent(long eventId) throws Exception {
        Event e = events.get(eventId);

        // check if null
        if (e == null) {
            throw new IllegalArgumentException(String.format("Event %d not found", eventId));
        }

        // check if it is in QUEUED state
        if (e.getStatus() != Event.Status.QUEUED) {
            throw new IllegalArgumentException("Event not in QUEUED state");
        }

        // if it is a MessageEvent and there is no connectivity between the nodes, drop it
        if (e instanceof MessageEvent m && !router.haveConnectivity(m.getSenderId(), m.getRecipientId())) {
            log.info("Dropped: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
            m.setStatus(Event.Status.DROPPED);
            return;
        }

        // deliver the event
        this.scenario.getSchedule().appendEvent(e);
        e.setStatus(Event.Status.DELIVERED);

        // For timeouts, this should be called before, so the Replica time is updated
        this.observers.forEach(o -> o.onEventDelivered(e));

        switch (e) {
            case ClientRequestEvent c -> {
                this.scenario.getNodes().get(c.getRecipientId()).handleMessage(c.getSenderId(), c.getPayload());
            }
            case MessageEvent m -> {
                this.scenario.getNodes().get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
            }
            case TimeoutEvent t -> {
                t.getTask().run();
            }
            default -> {
                throw new IllegalArgumentException("Unknown event type");
            }
        }

        log.info("Delivered " + e);
    }

    /**
     * Drops a message from the network.
     *
     * @param eventId The ID of the message to drop.
     */
    public synchronized void dropEvent(long eventId) {
        // check if event is a message
        Event e = events.get(eventId);

        if (e.getStatus() != Event.Status.QUEUED) {
            throw new IllegalArgumentException("Event not found or not in QUEUED state");
        }
        e.setStatus(Event.Status.DROPPED);
        this.observers.forEach(o -> o.onEventDropped(e));
        log.info("Dropped: " + e);
    }

    /**
     * Gets an event by ID.
     *
     * @param eventId The ID of the event to get.
     * @return The event with the given ID.
     */
    public synchronized Event getEvent(long eventId) {
        return events.get(eventId);
    }

    /**
     * Applies a mutation to a message and appends the fault event to the schedule.
     *
     * @param eventId The ID of the message to mutate.
     * @param fault   The fault to apply.
     */
    public synchronized void applyMutation(long eventId, Fault fault) {
        Event e = events.get(eventId);

        // check if event does not exist
        if (e == null) {
            throw new IllegalArgumentException(String.format("Event %d not found", eventId));
        }

        // check if mutator does not exist
        if (fault == null) {
            throw new IllegalArgumentException("Mutator not found");
        }

        // check if event is not in QUEUED state
        if (e.getStatus() != Event.Status.QUEUED) {
            throw new IllegalArgumentException("Message not found or not in QUEUED state");
        }

        // check it is a message event!
        if (!(e instanceof MessageEvent m)) {
            throw new IllegalArgumentException(String.format(
                    "Event %d is not a message - cannot mutate it.", eventId));
        }

        // create input for the fault
        FaultContext input = new FaultContext(this.scenario, e);

        // check if mutator can be applied to the event
        if (!fault.test(input)) {
            throw new IllegalArgumentException(
                    String.format("Mutator %s cannot be applied to event %d", fault.getId(), eventId)
            );
        }

        // apply the mutation
        fault.accept(input);
        scenario.markReplicaFaulty(m.getSenderId());

        // create a new event for the mutation
        MutateMessageEvent mutateMessageEvent = MutateMessageEvent.builder()
                .eventId(this.eventSeqNum.getAndIncrement())
                .senderId(m.getSenderId())
                .recipientId(m.getRecipientId())
                .payload(new MutateMessageEventPayload(eventId, fault.getId()))
                .build();
        this.appendEvent(mutateMessageEvent);

        // append the event to the schedule
        mutateMessageEvent.setStatus(Event.Status.DELIVERED);
        this.scenario.getSchedule().appendEvent(mutateMessageEvent);
        this.observers.forEach(o -> o.onMessageMutation(mutateMessageEvent.getPayload()));

        log.info("Mutated: " + m);
    }

    public synchronized void applyFault(String faultId) {
        Fault fault = this.networkFaults.get(faultId);
        if (fault == null) {
            throw new IllegalArgumentException("Fault not found: " + faultId);
        }
        this.applyFault(fault);
    }

    public synchronized void applyFault(Fault fault) {
        // create input for the fault
        FaultContext input = new FaultContext(this.scenario);

        // check if fault can be applied
        if (!fault.test(input)) {
            throw new IllegalArgumentException("Fault cannot be applied");
        }

        // apply the fault
        fault.accept(input);

        // create a new event for the fault and append it to the schedule
        GenericFaultEvent faultEvent = GenericFaultEvent.builder()
                .eventId(this.eventSeqNum.getAndIncrement())
                .payload(fault)
                .build();
        faultEvent.setStatus(Event.Status.DELIVERED);
        this.scenario.getSchedule().appendEvent(faultEvent);
        this.observers.forEach(o -> o.onFault(fault));
    }

    /**
     * Creates a new timeout event
     *
     * @param node     The node that created the timeout
     * @param runnable The task to execute when the timeout occurs
     * @param timeout  The timeout duration
     * @return The ID of the newly-created timeout event
     */
    public synchronized long setTimeout(Node node, Runnable runnable,
                                        Duration timeout) {
        TimeoutEvent timeoutEvent = TimeoutEvent.builder()
                .eventId(this.eventSeqNum.getAndIncrement())
                .description("TIMEOUT")
                .nodeId(node.getId())
                .timeout(timeout)
                .expiresAt(node.getCurrentTime().plus(timeout))
                .task(runnable)
                .build();
        this.appendEvent(timeoutEvent);
        this.observers.forEach(o -> o.onTimeout(timeoutEvent));
//        log.info("Timeout set for " + node.getId() + " in " + timeout + "ms: " + timeoutEvent);
        return timeoutEvent.getEventId();
    }

    /**
     * Clears a timeout event.
     *
     * @param eventId The ID of the event to clear.
     */
    public synchronized void clearTimeout(Node node, long eventId) {
        Event e = events.get(eventId);

        if (e == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }

        if (!(e instanceof TimeoutEvent timeoutEvent)) {
            throw new IllegalArgumentException("Event is not a timeout: " + eventId);
        }

        if (!timeoutEvent.getNodeId().equals(node.getId())) {
            throw new IllegalArgumentException("Timeout does not belong to this node!");
        }

        timeoutEvent.setStatus(Event.Status.DROPPED);
        this.observers.forEach(o -> o.onEventDropped(timeoutEvent));
    }

    /**
     * Gets all queued timeouts for a given node.
     *
     * @param node The node to get timeouts for.
     * @return A list of event IDs of queued timeouts.
     */
    public synchronized List<Long> getQueuedTimeouts(Node node) {
        return this.events.values()
                .stream()
                .filter(e -> e instanceof TimeoutEvent t &&
                        t.getNodeId().equals(node.getId()) &&
                        t.getStatus() == Event.Status.QUEUED)
                .map(Event::getEventId)
                .toList();
    }

    /**
     * Clears all timeouts for a given node.
     *
     * @param node The node to clear timeouts for.
     */
    public synchronized void clearReplicaTimeouts(Node node) {
        // get all event IDs for timeouts from this node
        List<Long> eventIds = this.getQueuedTimeouts(node);

        // remove all event IDs
        for (Long eventId : eventIds) {
            Event e = events.get(eventId);
            e.setStatus(Event.Status.DROPPED);
            this.observers.forEach(o -> o.onEventDropped(e));
        }
    }

    @JsonIgnore
    public synchronized SortedSet<String> getNodeIds() {
        return this.scenario.getNodes().keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public synchronized Node getNode(String nodeId) {
        return this.scenario.getNodes().get(nodeId);
    }

    @JsonIgnore
    public synchronized List<Fault> getEnabledNetworkFaults() {
        FaultContext input = new FaultContext(this.scenario);
        return this.networkFaults.values().stream()
                .filter(f -> f.test(input))
                .toList();
    }

    public synchronized Fault getNetworkFault(String faultId) {
        return this.networkFaults.get(faultId);
    }
}
