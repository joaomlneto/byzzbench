# Implementing new BFT Protocols

This document describes the steps to implement a new BFT protocol in ByzzBench.

A BFT Protocol implementation in ByzzBench consists of the following components:

- A protocol replica that *extends* [`Replica<T>`](../simulator/src/main/java/byzzbench/simulator/Replica.java): these
  are the nodes that run the protocol and communicate with each other
  via messages. `T` is the type of each of the entries in the commit log (this should be simplified!).
    - Example: [PBFT-Java Replica](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/PbftReplica.java)
    - The `Replica` constructor requires a `replicaId`, `nodeIds` (set of all replica IDs in the
      *cluster*), `transport` (the virtualized network instance) and a `commitLog` (instance where all committed
      operations from the replica are sent to).
- A set of protocol message POJOs that
  *implement* [`MessagePayload`](../simulator/src/main/java/byzzbench/simulator/transport/MessagePayload.java): these
  are the messages that are exchanged between
  replicas.
    - They just need to have a `String getType()` method that returns e.g. that it is a `PREPARE` message.
    - Examples from
      PBFT-Java: [Checkpoint](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/CheckpointMessage.java), [Commit](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/CommitMessage.java), [NewView](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/NewViewMessage.java), [Phase](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/PhaseMessage.java), [Prepare](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/PrepareMessage.java), [PrePrepare](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/PrePrepareMessage.java), [Reply](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/ReplyMessage.java), [Request](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/RequestMessage.java), [ViewChange](../simulator/src/main/java/byzzbench/simulator/protocols/pbft_java/message/ViewChangeMessage.java)

## Communication and Timeouts

Using the Actor model, replicas communicate with each other via
a [`Transport`](../simulator/src/main/java/byzzbench/simulator/transport) instance.
The `Transport` is a virtualized network that allows replicas to send messages to each other.
The `Transport` is responsible for delivering messages to the correct recipient, and also for handling timeouts.

Communication between replicas is done via message-passing through the `Transport`. The `Replica` superclass exposes a
few methods:

- `sendMessage`: send a message to a recipient
- `multicastMessage`: send a message to a set of replicas
- `broadcastMessage`: send a message to every other replica
- `broadcastMessageIncludingSelf`: send a message to every replica, including self

Timeouts are implemented via callbacks to a `Runnable`, also handled by the `Transport`:

- `setTimeout(Runnable, timeout)`: Creates a timeout event of `timeout` milliseconds, which will then invoke
  the `Runnable`. The time is currently being ignored, and is instead triggered just like any other event by the
  scheduler.
- `clearAllTimeouts()`: Invalidates all outstanding timeouts for the current replica.

## Commit Log

Each replica has its own instance of a `CommitLog`: an immutable and total ordered sequence of records. This is used to
check whether safety invariants of distributed consensus are broken.

## Pitfalls

- **Non-determinism**: For reproducibility we require the removal of any non-determinism! This can manifest itself in
  many ways:
    - Avoid interfaces and collections that do not guarantee order, such as `Set` or `Map`: use the `OrderedSet` or
      `OrderedMap` interfaces instead, and `TreeSet` or `LinkedHashMap` implementations to ensure order.
    - Avoid the use of Java's `CompletableFuture`: the implementation in Java is non-deterministic, as it uses a
      `ForkJoinPool`
      that can execute tasks in parallel - tasks that are submitted to the `CompletableFuture` can be executed in any
      order, and this can lead to non-deterministic behavior. Use our own `DeterministicCompletableFuture`, which will
      execute the tasks in the order they were submitted to it.

