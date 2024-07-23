package byzzbench.simulator.transport;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.NetworkFault;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

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
  /**
   * The sequence number for events.
   */
  private final AtomicLong eventSeqNum = new AtomicLong(1);

  /**
   * The sequence number for mutators.
   */
  private final AtomicLong mutatorSeqNum = new AtomicLong(1);

  /**
   * Map of node id to the {@link Replica} object.
   */
  @Getter(onMethod_ = { @Synchronized })
  @JsonIgnore
  private final Map<String, Replica<T>> nodes = new HashMap<>();

  /**
   * Map of client id to the {@link Client} object.
   */
  @Getter(onMethod_ = { @Synchronized })
  @JsonIgnore
  private final Map<String, Client<T>> clients = new HashMap<>();

  /**
   * Map of event ID to the {@link Event} object.
   */
  @Getter(onMethod_ = { @Synchronized })
  @JsonIgnore
  private final Map<Long, Event> events = new TreeMap<>();

  /**
   * The schedule of events in order of delivery.
   */
  @Getter(onMethod_ = { @Synchronized })
  private final List<Event> schedule = new ArrayList<>();

  /**
   * Map of mutator ID to the {@link MessageMutator} object.
   */
  @Getter(onMethod_ = { @Synchronized })
  @JsonIgnore
  private final Map<Long, MessageMutator> mutators = new TreeMap<>();

  @Getter
  // initialize with a NetworkFault fault
  private final List<Fault> faults =
      new ArrayList<>(List.of(new NetworkFault(this)));

  /**
   * Map of node id to the partition ID that the node is in.
   * Two nodes on different partitions cannot communicate.
   * Nodes without partition IDs are in the same "null" partition and can
   * communicate with each other.
   */
  @Getter(onMethod_ = { @Synchronized })
  private final Map<String, Integer> partitions = new HashMap<>();

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
   * Removes a node from the system.
   *
   * @param replicaId
   */
  public void removeNode(String replicaId) { nodes.remove(replicaId); }

  public void removeNode(Replica<T> replica) {
    nodes.remove(replica.getNodeId());
  }

  public void sendClientRequest(String sender, Serializable operation,
                                String recipient) {
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

  public void sendClientResponse(String sender, Serializable response,
                                 String recipient) {
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

  public void sendMessage(String sender, MessagePayload message,
                          String recipient) {
    this.multicast(sender, Set.of(recipient), message);
  }

  public void reset() {
    this.eventSeqNum.set(1);
    this.nodes.clear();
    this.events.clear();
    this.mutators.clear();
    this.schedule.clear();
    this.nodes.values().forEach(Replica::initialize);
  }

  public List<Event> getEventsInState(Event.Status status) {
    return this.events.values()
        .stream()
        .filter(m -> m.getStatus() == status)
        .toList();
  }

  public void multicast(String sender, Set<String> recipients,
                        MessagePayload payload) {
    for (String recipient : recipients) {
      long messageId = this.eventSeqNum.getAndIncrement();
      MessageEvent messageEvent =
          new MessageEvent(messageId, sender, recipient, payload);
      events.put(messageId, messageEvent);

      // go through the faults
      for (Fault fault : faults) {
        fault.apply(messageEvent);
      }
    }
  }

  public void deliverEvent(long eventId) throws Exception {
    // check if event is a message
    Event e = events.get(eventId);

    // check if null
    if (e == null) {
      throw new RuntimeException(String.format("Event %d not found", eventId));
    }

    // check if it is in QUEUED state
    if (e.getStatus() != Event.Status.QUEUED) {
      throw new RuntimeException("Event not in QUEUED state");
    }

    // deliver the event
    this.schedule.add(e);
    e.setStatus(Event.Status.DELIVERED);

    switch (e) {
            case ClientRequestEvent c ->
                    nodes.get(c.getRecipientId()).handleClientRequest(c.getSenderId(), c.getPayload());
            case MessageEvent m -> nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
            case TimeoutEvent t -> t.getTask().run();
            default -> throw new RuntimeException("Unknown event type");
        }

        log.info("Delivered " + e.getEventId() + ": " + e.getSenderId() + "->" + e.getRecipientId());
    }

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

    public void registerMessageMutator(Class<? extends Serializable> messageClass, MessageMutator mutator) {
        this.mutators.put(mutatorSeqNum.getAndIncrement(), mutator);
    }

    public void registerMessageMutators(MessageMutatorFactory factory) {
        for (MessageMutator mutator : factory.mutators()) {
                for (Class<? extends Serializable> clazz :
                     mutator.getInputClasses()) {
                  this.registerMessageMutator(clazz, mutator);
                }
              }
              log.info("Registered Message Mutators:" + this.mutators);
            }

            public List<Map.Entry<Long, MessageMutator>> getEventMutators(
                long eventId) {
              Event e = events.get(eventId);

              // if event is null, throw an exception
              if (e == null) {
                throw new RuntimeException(
                    String.format("Event %d not found", eventId));
              }

              // if it is not a MessageEvent, return an empty list
              if (!(e instanceof MessageEvent messageEvent)) {
                return List.of();
              }

              List<Map.Entry<Long, MessageMutator>> messageMutators =
                  mutators.entrySet()
                      .stream()
                      // filter mutators that can be applied to the event
                      .filter(entry
                              -> entry.getValue().getInputClasses().contains(
                                  messageEvent.getPayload().getClass()))
                      .toList();

              // return their keys
              return messageMutators;
            }

            public void applyMutation(long eventId, long mutatorId) {
              Event e = events.get(eventId);
              MessageMutator mutator = mutators.get(mutatorId);

              if (e.getStatus() != Event.Status.QUEUED) {
                throw new RuntimeException(
                    "Message not found or not in QUEUED state");
              }

              if (mutator == null) {
                throw new RuntimeException("Mutator not found");
              }

              // check if mutator can be applied to the event
              if (!mutator.getInputClasses().contains(
                      ((MessageEvent)e).getPayload().getClass())) {
                throw new RuntimeException(
                    "Mutator cannot be applied to the event");
              }

              // check it is a message event!
              if (!(e instanceof MessageEvent m)) {
                throw new RuntimeException(String.format(
                    "Event %d is not a message - cannot mutate it.", eventId));
              }

              Serializable newPayload = mutator.apply(m.getPayload());
              // FIXME: the typecasting here to MessagePayload is very nasty
              MessageEvent newMessage = new MessageEvent(
                  m.getEventId(), m.getSenderId(), m.getRecipientId(),
                  (MessagePayload)newPayload);
              events.put(eventId, newMessage);
              log.info("Mutated: " + m.getSenderId() + "->" +
                       m.getRecipientId() + ": " + m.getPayload() + " -> " +
                       newPayload);
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

            public void addFault(Fault fault) { this.faults.add(fault); }

            public void addFaults(List<Fault> faults) {
              this.faults.addAll(faults);
            }

            public Set<String> getNodeIds() { return nodes.keySet(); }

            public Replica<T> getNode(String nodeId) {
              return nodes.get(nodeId);
            }
  }
