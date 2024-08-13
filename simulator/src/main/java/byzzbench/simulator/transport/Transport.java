package byzzbench.simulator.transport;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.Schedule;
import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transport layer for the simulator.
 * <p>
 * This class is responsible for handling events (messages and timeouts).
 * It also provides methods for sending messages, setting timeouts, and applying
 * faults.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Log
public class Transport<T extends Serializable> {
    private final ScenarioExecutor<T> scenario;

    /**
     * The service for storing and managing schedules.
     */
    private final MessageMutatorService messageMutatorService;

    /**
     * The service for storing and managing schedules.
     */
    private final SchedulesService schedulesService;

    /**
     * The sequence number for events.
     */
    private final AtomicLong eventSeqNum = new AtomicLong(1);

    /**
     * Map of node id to the {@link Replica} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<String, Replica<T>> nodes = new HashMap<>();

    /**
     * Map of client id to the {@link Client} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<String, Client<T>> clients = new HashMap<>();

    /**
     * Map of event ID to the {@link Event} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<Long, Event> events = new TreeMap<>();

    /**
     * The router for managing partitions.
     */
    private final Router router = new Router();

    /**
     * The schedule of events in order of delivery.
     */
    @Getter(onMethod_ = {@Synchronized})
    private Schedule schedule;

    public Transport (ScenarioExecutor<T> scenario, MessageMutatorService messageMutatorService, SchedulesService schedulesService) {
        this.scenario = scenario;
        this.messageMutatorService = messageMutatorService;
        this.schedulesService = schedulesService;
        this.schedule = this.schedulesService.addSchedule();
    }

    /**
     * Registers a new node in the system.
     *
     * @param replica The node to add.
     */
    public void addNode(Replica<T> replica) {
        nodes.put(replica.getNodeId(), replica);
    }

    /**
     * Registers a new client in the system.
     *
     * @param client The client to add.
     */
    public void addClient(Client<T> client) {
        clients.put(client.getClientId(), client);
    }

    /**
     * Creates a number of clients in the system.
     *
     * @param numClients The number of clients to create.
     */
    public void createClients(int numClients) {
        for (int i = 0; i < numClients; i++) {
            Client<T> client = new Client<>(String.format("C%d", i), this);
            this.addClient(client);
        }
    }

    /**
     * Sends a request from a client to a replica.
     * @param sender The ID of the client sending the request
     * @param operation The operation to send
     * @param recipient The ID of the replica receiving the request
     */
    public void sendClientRequest(String sender, Serializable operation, String recipient) {
        // assert that the sender exists
        if (!clients.containsKey(sender)) {
            throw new RuntimeException("Client not found: " + sender);
        }

        if (!nodes.containsKey(recipient)) {
            throw new RuntimeException("Replica not found: " + recipient);
        }

        long eventId = this.eventSeqNum.getAndIncrement();
        Event event = new ClientRequestEvent(eventId, sender, recipient, operation);
        events.put(eventId, event);
    }

    /**
     * Sends a response from a replica to a client.
     * @param sender The ID of the replica sending the response
     * @param response The response to send
     * @param recipient The ID of the client receiving the response
     */
    public void sendClientResponse(String sender, Serializable response, String recipient) {
        // assert that the sender exists
        if (!nodes.containsKey(sender)) {
            throw new RuntimeException("Replica not found: " + sender);
        }

        if (!clients.containsKey(recipient)) {
            throw new RuntimeException("Client not found: " + recipient);
        }

        long eventId = this.eventSeqNum.getAndIncrement();
        Event event = new ClientReplyEvent(eventId, sender, recipient, response);
        events.put(eventId, event);
    }

    /**
     * Sends a message between two replicas.
     * @param sender The ID of the replica sending the message
     * @param message The payload of the message to send
     * @param recipient The ID of the replica receiving the message
     */
    public void sendMessage(String sender, MessagePayload message,
                            String recipient) {
        this.multicast(sender, Set.of(recipient), message);
    }

    /**
     * Resets the transport layer.
     */
    public void reset() {
        this.eventSeqNum.set(1);
        this.nodes.clear();
        this.events.clear();
        this.nodes.values().forEach(Replica::initialize);
        this.schedule = schedulesService.addSchedule();
    }

    /**
     * Gets all events in a given state.
     * @param status The state to filter by
     * @return A list of events in the given state
     */
    public List<Event> getEventsInState(Event.Status status) {
        return this.events.values()
                .stream()
                .filter(m -> m.getStatus() == status)
                .toList();
    }

    /**
     * Multicasts a message to a set of recipients.
     * @param sender The ID of the sender
     * @param recipients The set of recipient IDs
     * @param payload The payload of the message
     */
    public void multicast(String sender, Set<String> recipients,
                          MessagePayload payload) {
        for (String recipient : recipients) {
            long messageId = this.eventSeqNum.getAndIncrement();
            MessageEvent messageEvent =
                    new MessageEvent(messageId, sender, recipient, payload);
            events.put(messageId, messageEvent);
        }
    }

    public synchronized void deliverEvent(long eventId) throws Exception {
        Event e = events.get(eventId);

        // check if null
        if (e == null) {
            throw new RuntimeException(String.format("Event %d not found", eventId));
        }

        // check if it is in QUEUED state
        if (e.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Event not in QUEUED state");
        }

        // if it is a MessageEvent and there is no connectivity between the nodes, drop it
        if (e instanceof MessageEvent m && !router.haveConnectivity(m.getSenderId(), m.getRecipientId())) {
            log.info("Dropped: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
            m.setStatus(Event.Status.DROPPED);
            return;
        }

        // deliver the event
        this.schedule.appendEvent(e);
        e.setStatus(Event.Status.DELIVERED);

        switch (e) {
            case ClientRequestEvent c -> {
                nodes.get(c.getRecipientId()).handleClientRequest(c.getSenderId(), c.getPayload());
            }
            case ClientReplyEvent c -> {
                clients.get(c.getRecipientId()).handleReply(c.getSenderId(), c.getPayload());
            }
            case MessageEvent m -> {
                nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
            }
            case TimeoutEvent t -> {
                t.getTask().run();
            }
            default -> {
                throw new RuntimeException("Unknown event type");
            }
        }

        log.info("Delivered " + e.getEventId() + ": " + e.getSenderId() + "->" + e.getRecipientId());
    }

    /**
     * Drops a message from the network.
     * @param messageId The ID of the message to drop.
     */
    public void dropMessage(long messageId) {
        // check if event is a message
        Event e = events.get(messageId);

        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format("Event %d is not a message", messageId));
        }

        if (m.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }
        m.setStatus(Event.Status.DROPPED);
        log.info("Dropped: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
    }

    /**
     * Gets an event by ID.
     * @param eventId The ID of the event to get.
     * @return The event with the given ID.
     */
    public Event getEvent(long eventId) {
        return events.get(eventId);
    }

    public synchronized void applyMutation(long eventId, String mutatorId) {
        Event e = events.get(eventId);
        MessageMutationFault mutator = this.messageMutatorService.getMutator(mutatorId);

        // check if event does not exist
        if (e == null) {
            throw new RuntimeException(String.format("Event %d not found", eventId));
        }

        // check if mutator does not exist
        if (mutator == null) {
            throw new RuntimeException("Mutator not found");
        }

        // check if event is not in QUEUED state
        if (e.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }

        // check it is a message event!
        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format(
                    "Event %d is not a message - cannot mutate it.", eventId));
        }

        // check if mutator can be applied to the event
        if (!mutator.canMutate(m.getPayload())) {
            throw new RuntimeException(
                    String.format("Mutator %s cannot be applied to event %d", mutatorId, eventId)
            );
        }

        // apply the mutation
        mutator.accept(new FaultInput<T>(this.scenario, m));

        // create a new event for the mutation
        Event mutateMessageEvent = new MutateMessageEvent(
                this.eventSeqNum.getAndIncrement(), m.getSenderId(),
                m.getRecipientId(), new MutateMessageEventPayload(
                        eventId, mutatorId));
        events.put(mutateMessageEvent.getEventId(), mutateMessageEvent);

        // append the event to the schedule
        mutateMessageEvent.setStatus(Event.Status.DELIVERED);
        this.schedule.appendEvent(mutateMessageEvent);

        log.info("Mutated: " + m);
    }

    public long setTimeout(Replica<T> replica, Runnable runnable,
                           long timeout) {
        Event e = new TimeoutEvent(this.eventSeqNum.getAndIncrement(),
                "TIMEOUT", replica.getNodeId(),
                timeout, runnable);
        this.events.put(e.getEventId(), e);
        log.info("Timeout set for " + replica.getNodeId() + " in " +
                timeout + "ms: " + e);
        return e.getEventId();
    }

    public void clearReplicaTimeouts(Replica<T> replica) {
        // get all event IDs for timeouts from this replica
        List<Long> eventIds =
                this.events.values()
                        .stream()
                        .filter(
                                e
                                        -> e instanceof TimeoutEvent t &&
                                        t.getSenderId().equals(replica.getNodeId()) &&
                                        t.getStatus() == Event.Status.QUEUED)
                        .map(Event::getEventId)
                        .toList();

        // remove all event IDs
        for (Long eventId : eventIds) {
            this.events.remove(eventId);
        }
    }

    public Set<String> getNodeIds() {
        return nodes.keySet();
    }

    public Replica<T> getNode(String nodeId) {
        return nodes.get(nodeId);
    }
}
